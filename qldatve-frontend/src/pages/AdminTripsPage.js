import React, { useEffect, useState } from 'react';
import api from '../api';
// --- IMPORT MUI COMPONENTS ---
import {
    Button,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    IconButton,
    TextField,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Typography,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Box // For layout inside Dialog
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';
// ----------------------------

function AdminTripsPage() {
    const [trips, setTrips] = useState([]); // State for trips list
    const [routes, setRoutes] = useState([]); // State for routes dropdown
    const [vehicles, setVehicles] = useState([]); // State for vehicles dropdown
    const [error, setError] = useState(''); // State for error messages

    // --- Dialog/Modal States ---
    const [modalIsOpen, setModalIsOpen] = useState(false); // Create/Edit dialog open state
    const [currentTrip, setCurrentTrip] = useState({ routeId: '', vehicleId: '', departureTime: '', arrivalTime: '', basePrice: '' }); // Form data
    const [isEditing, setIsEditing] = useState(false); // Is it an edit or create operation?
    const [editTripId, setEditTripId] = useState(null); // ID of the trip being edited
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false); // Delete confirm dialog state
    const [tripToDelete, setTripToDelete] = useState(null); // ID of trip to be deleted

    // --- Fetch Data ---
    useEffect(() => {
        fetchTrips();
        fetchRoutes();
        fetchVehicles();
    }, []); // Run once on component mount

    const fetchTrips = async () => {
        try {
            setError('');
            const response = await api.get('/admin/trips');
            console.log("AdminTripsPage - Trips API Response:", response.data);
            setTrips(Array.isArray(response.data) ? response.data : []); // Ensure it's an array
        } catch (err) {
            console.error("Lỗi khi tải chuyến đi:", err);
            setError("Không thể tải dữ liệu chuyến đi.");
        }
    };

    const fetchRoutes = async () => {
        try {
            const response = await api.get('/admin/routes');
            console.log("AdminTripsPage - Routes API Response:", response.data);
            setRoutes(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("Lỗi khi tải tuyến đường:", err);
            // Don't set main error, let fetchTrips handle it if needed
        }
    };

    const fetchVehicles = async () => {
        try {
            const response = await api.get('/admin/vehicles');
             console.log("AdminTripsPage - Vehicles API Response:", response.data);
            setVehicles(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("Lỗi khi tải xe:", err);
        }
    };

    // --- Dialog Handling ---
    const openModalForCreate = () => {
        setIsEditing(false);
        setCurrentTrip({ routeId: '', vehicleId: '', departureTime: '', arrivalTime: '', basePrice: '' });
        setError(''); // Clear previous dialog errors
        setModalIsOpen(true);
    };

    const openModalForEdit = (trip) => {
        setIsEditing(true);
        setEditTripId(trip.tripId);
        setCurrentTrip({
            routeId: trip.route?.routeId || '',
            vehicleId: trip.vehicle?.vehicleId || '',
            // Use formatter for date inputs
            departureTime: formatDateTimeLocal(trip.departureTime),
            arrivalTime: formatDateTimeLocal(trip.arrivalTime),
            basePrice: trip.basePrice || ''
        });
        setError('');
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
        setError(''); // Also clear errors when closing manually
    };

    // --- Form Input Handling ---
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        // Handle Select component specifically if needed, but it should work
        setCurrentTrip(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    // --- API Call: Create/Update ---
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError(''); // Clear previous errors

        // Basic frontend validation
        if (!currentTrip.routeId || !currentTrip.vehicleId || !currentTrip.departureTime || !currentTrip.arrivalTime || currentTrip.basePrice === '') {
            setError("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        try {
             // Prepare payload for backend (ensure types are correct)
             const payload = {
                routeId: parseInt(currentTrip.routeId, 10),
                vehicleId: parseInt(currentTrip.vehicleId, 10),
                // Send ISO string directly if input is datetime-local
                departureTime: currentTrip.departureTime + ":00", // Add seconds if needed by backend
                arrivalTime: currentTrip.arrivalTime + ":00",   // Add seconds if needed by backend
                basePrice: parseFloat(currentTrip.basePrice)
            };
            console.log("Submitting payload:", payload); // Log payload before sending

            if (isEditing) {
                await api.put(`/admin/trips/${editTripId}`, payload);
            } else {
                await api.post('/admin/trips', payload);
            }
            fetchTrips(); // Reload data
            closeModal(); // Close dialog on success
        } catch (err) {
            console.error("Lỗi khi lưu chuyến đi:", err.response || err);
            setError(err.response?.data?.message || err.response?.data || "Lỗi khi lưu chuyến đi.");
            // Keep the dialog open to show the error
        }
    };

    // --- Delete Confirmation Handling ---
     const openConfirmDeleteDialog = (tripId) => {
        setTripToDelete(tripId);
        setError(''); // Clear previous errors
        setConfirmDeleteDialogOpen(true);
    };

    const closeConfirmDeleteDialog = () => {
        setTripToDelete(null);
        setConfirmDeleteDialogOpen(false);
        setError(''); // Clear error when closing
    };

    const handleDeleteConfirm = async () => {
        if (tripToDelete) {
            try {
                setError('');
                await api.delete(`/admin/trips/${tripToDelete}`);
                fetchTrips(); // Reload data
                closeConfirmDeleteDialog(); // Close dialog on success
            } catch (err) {
                console.error("Lỗi khi xóa chuyến đi:", err.response || err);
                 if (err.response?.data && typeof err.response.data === 'string' && err.response.data.includes('violates foreign key constraint')) {
                     setError("Lỗi khi xóa: Chuyến đi này đã có vé được đặt.");
                 } else {
                    setError(err.response?.data?.message || err.response?.data || "Lỗi khi xóa chuyến đi.");
                 }
                // Keep the dialog open to show the error
            }
        }
     };

    // --- Helper Function for Date Formatting ---
    const formatDateTimeLocal = (dateTimeString) => {
        if (!dateTimeString) return '';
        try {
            // Assuming dateTimeString is already in a format that Date can parse correctly
            // or is directly from backend (potentially ISO string)
            const date = new Date(dateTimeString);
            if (isNaN(date.getTime())) return ''; // Invalid date

            // Convert to YYYY-MM-DDTHH:mm format required by datetime-local input
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');

            return `${year}-${month}-${day}T${hours}:${minutes}`;
        } catch (e) {
            console.error("Error formatting date:", dateTimeString, e);
            return '';
        }
    };

    console.log("AdminTripsPage - Rendering with trips state:", trips); // Check state before render

    return (
        <Paper sx={{ p: 2, margin: 'auto', maxWidth: 1200, flexGrow: 1 }}>
            <Typography variant="h6" gutterBottom component="div">
                Quản lý Chuyến đi
            </Typography>

            <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openModalForCreate}
                sx={{ mb: 2 }}
            >
                Tạo Chuyến đi mới
            </Button>

             {/* Display general errors */}
             {error && !modalIsOpen && !confirmDeleteDialogOpen && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="trips table">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Tuyến</TableCell>
                            <TableCell>Xe</TableCell>
                            <TableCell>Giờ đi</TableCell>
                            <TableCell>Giờ đến</TableCell>
                            <TableCell>Giá vé (VND)</TableCell>
                            <TableCell align="right">Hành động</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {/* Check if trips is an array and has data */}
                        {Array.isArray(trips) && trips.length > 0 ? (
                            trips.map((trip) => (
                                <TableRow
                                    key={trip.tripId}
                                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                >
                                    <TableCell component="th" scope="row">{trip.tripId}</TableCell>
                                    <TableCell>{trip.route?.startLocation} - {trip.route?.endLocation}</TableCell>
                                    <TableCell>{trip.vehicle?.licensePlate} ({trip.vehicle?.vehicleType})</TableCell>
                                    <TableCell>{new Date(trip.departureTime).toLocaleString()}</TableCell>
                                    <TableCell>{new Date(trip.arrivalTime).toLocaleString()}</TableCell>
                                    <TableCell>{trip.basePrice?.toLocaleString()}</TableCell>
                                    <TableCell align="right">
                                        <IconButton color="primary" onClick={() => openModalForEdit(trip)}>
                                            <EditIcon />
                                        </IconButton>
                                        <IconButton color="error" onClick={() => openConfirmDeleteDialog(trip.tripId)}>
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={7} align="center">Không có dữ liệu chuyến đi.</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Edit/Create Dialog */}
            <Dialog open={modalIsOpen} onClose={closeModal} fullWidth maxWidth="sm">
                <DialogTitle>{isEditing ? "Sửa Chuyến đi" : "Tạo Chuyến đi mới"}</DialogTitle>
                <DialogContent>
                    {/* Display errors inside the dialog */}
                    {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <Box component="form" onSubmit={handleFormSubmit} noValidate sx={{ mt: 1 }}>
                        <FormControl fullWidth margin="dense" required error={!currentTrip.routeId && !!error}>
                            <InputLabel id="route-select-label">Tuyến đường</InputLabel>
                            <Select
                                labelId="route-select-label"
                                label="Tuyến đường"
                                name="routeId"
                                value={currentTrip.routeId}
                                onChange={handleInputChange}
                            >
                                <MenuItem value=""><em>-- Chọn tuyến --</em></MenuItem>
                                {routes.map(route => (
                                    <MenuItem key={route.routeId} value={route.routeId}>
                                        {route.startLocation} - {route.endLocation}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <FormControl fullWidth margin="dense" required error={!currentTrip.vehicleId && !!error}>
                            <InputLabel id="vehicle-select-label">Xe</InputLabel>
                            <Select
                                labelId="vehicle-select-label"
                                label="Xe"
                                name="vehicleId"
                                value={currentTrip.vehicleId}
                                onChange={handleInputChange}
                            >
                                 <MenuItem value=""><em>-- Chọn xe --</em></MenuItem>
                                {vehicles.map(vehicle => (
                                    <MenuItem key={vehicle.vehicleId} value={vehicle.vehicleId}>
                                        {vehicle.licensePlate} ({vehicle.vehicleType} - {vehicle.totalSeats} ghế)
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                         <TextField
                            margin="dense"
                            name="departureTime"
                            label="Thời gian đi"
                            type="datetime-local"
                            fullWidth
                            variant="outlined"
                            value={currentTrip.departureTime} // Value should be in YYYY-MM-DDTHH:mm
                            onChange={handleInputChange}
                            required
                            error={!currentTrip.departureTime && !!error}
                            InputLabelProps={{ shrink: true }}
                        />

                         <TextField
                            margin="dense"
                            name="arrivalTime"
                            label="Thời gian đến"
                            type="datetime-local"
                            fullWidth
                            variant="outlined"
                            value={currentTrip.arrivalTime} // Value should be in YYYY-MM-DDTHH:mm
                            onChange={handleInputChange}
                            required
                            error={!currentTrip.arrivalTime && !!error}
                            InputLabelProps={{ shrink: true }}
                        />

                        <TextField
                            margin="dense"
                            name="basePrice"
                            label="Giá vé cơ bản (VND)"
                            type="number"
                            fullWidth
                            variant="outlined"
                            value={currentTrip.basePrice}
                            onChange={handleInputChange}
                            required
                            error={currentTrip.basePrice === '' && !!error}
                            inputProps={{ min: 0, step: 1000 }}
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeModal}>Hủy</Button>
                    {/* Submit button is outside the form Box but triggers onSubmit */}
                    <Button onClick={handleFormSubmit} variant="contained">
                        {isEditing ? 'Lưu thay đổi' : 'Tạo mới'}
                    </Button>
                </DialogActions>
            </Dialog>

             {/* Delete Confirmation Dialog */}
             <Dialog
                open={confirmDeleteDialogOpen}
                onClose={closeConfirmDeleteDialog}
            >
                 <DialogTitle>Xác nhận Xóa</DialogTitle>
                <DialogContent>
                     {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <DialogContentText>
                        Bạn có chắc chắn muốn xóa chuyến đi ID {tripToDelete}? Hành động này không thể hoàn tác và có thể lỗi nếu đã có vé đặt.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeConfirmDeleteDialog}>Hủy</Button>
                    <Button onClick={handleDeleteConfirm} color="error" variant="contained">
                        Xóa
                    </Button>
                </DialogActions>
            </Dialog>
        </Paper>
    );
}

export default AdminTripsPage;