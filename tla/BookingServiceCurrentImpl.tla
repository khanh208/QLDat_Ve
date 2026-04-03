----------------------------- MODULE BookingServiceCurrentImpl -----------------------------
EXTENDS Naturals, FiniteSets

\* Muc tieu:
\* - Mo hinh hoa gan voi hanh vi hien tai cua BookingService.
\* - Tach 2 buoc "check ghe trung" va "commit booking" de mo phong race condition.
\* - Cho phep kiem tra 2 nhom thuoc tinh:
\*   + Safety: khong co 2 booking giu/ban cung 1 ghe.
\*   + Liveness: booking PENDING cuoi cung phai duoc giai quyet.

CONSTANTS Seats, Users, RequestIds, PendingTimeout

ASSUME /\ IsFiniteSet(Seats)
       /\ IsFiniteSet(Users)
       /\ IsFiniteSet(RequestIds)
       /\ Seats # {}
       /\ Users # {}
       /\ RequestIds # {}
       /\ PendingTimeout \in Nat
       /\ PendingTimeout > 0

PhaseValues == {"IDLE", "CHECKED", "DONE"}
StatusValues == {"NONE", "PENDING", "CONFIRMED", "CANCELLED"}
PaymentValues == {"CASH", "MOMO"}
SeatRequests == {ss \in SUBSET Seats : ss # {}}

DefaultUser == CHOOSE u \in Users : TRUE

VARIABLES
    now,
    phase,
    reqUser,
    reqSeats,
    reqPayment,
    bookingStatus,
    bookingCreatedAt

vars ==
    <<now, phase, reqUser, reqSeats, reqPayment, bookingStatus, bookingCreatedAt>>

Init ==
    /\ now = 0
    /\ phase = [id \in RequestIds |-> "IDLE"]
    /\ reqUser = [id \in RequestIds |-> DefaultUser]
    /\ reqSeats = [id \in RequestIds |-> {}]
    /\ reqPayment = [id \in RequestIds |-> "CASH"]
    /\ bookingStatus = [id \in RequestIds |-> "NONE"]
    /\ bookingCreatedAt = [id \in RequestIds |-> 0]

HeldBookingIds ==
    {id \in RequestIds :
        phase[id] = "DONE"
        /\ bookingStatus[id] \in {"PENDING", "CONFIRMED"}}

ConfirmedBookingIds ==
    {id \in RequestIds :
        phase[id] = "DONE"
        /\ bookingStatus[id] = "CONFIRMED"}

SeatHolders(seat) ==
    {id \in HeldBookingIds : seat \in reqSeats[id]}

ConfirmedSeatOwners(seat) ==
    {id \in ConfirmedBookingIds : seat \in reqSeats[id]}

CanPassDuplicateCheck(ss) ==
    /\ ss \in SeatRequests
    /\ \A seat \in ss : Cardinality(SeatHolders(seat)) = 0

\* Buoc nay dai dien cho phan:
\* - kiem tra request hop le
\* - kiem tra ghe dang PENDING/CONFIRMED
\* Neu qua duoc thi request duoc dua vao trang thai CHECKED.
StartCheck(id, user, ss, payment) ==
    /\ id \in RequestIds
    /\ phase[id] = "IDLE"
    /\ user \in Users
    /\ CanPassDuplicateCheck(ss)
    /\ payment \in PaymentValues
    /\ phase' = [phase EXCEPT ![id] = "CHECKED"]
    /\ reqUser' = [reqUser EXCEPT ![id] = user]
    /\ reqSeats' = [reqSeats EXCEPT ![id] = ss]
    /\ reqPayment' = [reqPayment EXCEPT ![id] = payment]
    /\ UNCHANGED <<now, bookingStatus, bookingCreatedAt>>

\* Buoc nay dai dien cho bookingRepository.save(...).
\* Co y khong kiem tra lai duplicate de mo phong bug race condition hien tai.
CommitBooking(id) ==
    /\ id \in RequestIds
    /\ phase[id] = "CHECKED"
    /\ phase' = [phase EXCEPT ![id] = "DONE"]
    /\ bookingStatus' =
        [bookingStatus EXCEPT
            ![id] = IF reqPayment[id] = "CASH" THEN "CONFIRMED" ELSE "PENDING"]
    /\ bookingCreatedAt' = [bookingCreatedAt EXCEPT ![id] = now]
    /\ UNCHANGED <<now, reqUser, reqSeats, reqPayment>>

\* Dai dien cho callback confirm thanh toan MoMo.
ConfirmBooking(id) ==
    /\ id \in RequestIds
    /\ phase[id] = "DONE"
    /\ bookingStatus[id] = "PENDING"
    /\ bookingStatus' = [bookingStatus EXCEPT ![id] = "CONFIRMED"]
    /\ UNCHANGED <<now, phase, reqUser, reqSeats, reqPayment, bookingCreatedAt>>

\* Dai dien cho cron job huy booking PENDING qua han.
CancelExpiredPending(id) ==
    /\ id \in RequestIds
    /\ phase[id] = "DONE"
    /\ bookingStatus[id] = "PENDING"
    /\ now >= bookingCreatedAt[id] + PendingTimeout
    /\ bookingStatus' = [bookingStatus EXCEPT ![id] = "CANCELLED"]
    /\ UNCHANGED <<now, phase, reqUser, reqSeats, reqPayment, bookingCreatedAt>>

\* Dai dien cho user tu huy booking.
UserCancelBooking(id) ==
    /\ id \in RequestIds
    /\ phase[id] = "DONE"
    /\ bookingStatus[id] \in {"PENDING", "CONFIRMED"}
    /\ bookingStatus' = [bookingStatus EXCEPT ![id] = "CANCELLED"]
    /\ UNCHANGED <<now, phase, reqUser, reqSeats, reqPayment, bookingCreatedAt>>

\* Dai dien cho admin huy booking.
AdminCancelBooking(id) ==
    /\ id \in RequestIds
    /\ phase[id] = "DONE"
    /\ bookingStatus[id] \in {"PENDING", "CONFIRMED"}
    /\ bookingStatus' = [bookingStatus EXCEPT ![id] = "CANCELLED"]
    /\ UNCHANGED <<now, phase, reqUser, reqSeats, reqPayment, bookingCreatedAt>>

AdvanceTime ==
    /\ now' = now + 1
    /\ UNCHANGED <<phase, reqUser, reqSeats, reqPayment, bookingStatus, bookingCreatedAt>>

Next ==
    \/ \E id \in RequestIds, user \in Users, ss \in SeatRequests, payment \in PaymentValues :
           StartCheck(id, user, ss, payment)
    \/ \E id \in RequestIds : CommitBooking(id)
    \/ \E id \in RequestIds : ConfirmBooking(id)
    \/ \E id \in RequestIds : CancelExpiredPending(id)
    \/ \E id \in RequestIds : UserCancelBooking(id)
    \/ \E id \in RequestIds : AdminCancelBooking(id)
    \/ AdvanceTime

TypeOK ==
    /\ now \in Nat
    /\ phase \in [RequestIds -> PhaseValues]
    /\ reqUser \in [RequestIds -> Users]
    /\ reqSeats \in [RequestIds -> SUBSET Seats]
    /\ reqPayment \in [RequestIds -> PaymentValues]
    /\ bookingStatus \in [RequestIds -> StatusValues]
    /\ bookingCreatedAt \in [RequestIds -> Nat]
    /\ \A id \in RequestIds :
         (phase[id] = "IDLE") => bookingStatus[id] = "NONE"

\* Safety manh hon hanh vi service mong muon:
\* cung 1 ghe khong duoc o trang thai PENDING/CONFIRMED o hon 1 booking.
NoSeatConflictHeld ==
    \A seat \in Seats : Cardinality(SeatHolders(seat)) <= 1

\* Safety hep hon:
\* cung 1 ghe khong duoc CONFIRMED cho hon 1 booking.
NoSeatConflictConfirmed ==
    \A seat \in Seats : Cardinality(ConfirmedSeatOwners(seat)) <= 1

\* Liveness:
\* moi booking dang PENDING cuoi cung phai duoc giai quyet
\* (hoac CONFIRMED, hoac CANCELLED).
PendingEventuallyResolved ==
    \A id \in RequestIds :
        [](
            (phase[id] = "DONE" /\ bookingStatus[id] = "PENDING")
            => <> (bookingStatus[id] = "CONFIRMED" \/ bookingStatus[id] = "CANCELLED")
        )

Spec ==
    /\ Init
    /\ [][Next]_vars
    /\ WF_vars(AdvanceTime)
    /\ \A id \in RequestIds : WF_vars(CancelExpiredPending(id))

===========================================================================================
