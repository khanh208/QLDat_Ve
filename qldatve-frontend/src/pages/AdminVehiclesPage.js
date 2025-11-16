import React, { useEffect, useState } from 'react';
import api from '../api';
// --- 1. THÊM IMPORTS TỪ MUI ---
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
    Typography
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';
// ------------------------------

// No need for Modal.setAppElement('#root');

function AdminVehiclesPage() {
    const [vehicles, setVehicles] = useState([]);
    const [error, setError] = useState('');
    const [modalIsOpen, setModalIsOpen] = useState(false);
    const [currentVehicle, setCurrentVehicle] = useState({ licensePlate: '', vehicleType: '', totalSeats: 0 });
    const [isEditing, setIsEditing] = useState(false);
    const [editVehicleId, setEditVehicleId] = useState(null);
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);
    const [vehicleToDelete, setVehicleToDelete] = useState(null);

    useEffect(() => {
        fetchVehicles();
    }, []);

    const fetchVehicles = async () => {
        try {
            setError('');
            const response = await api.get('/admin/vehicles');
            setVehicles(response.data);
        } catch (err) {
            console.error("Lỗi khi tải xe:", err);
            setError("Không thể tải dữ liệu xe.");
        }
    };

    const openModalForCreate = () => {
        setIsEditing(false);
        setCurrentVehicle({ licensePlate: '', vehicleType: '', totalSeats: 0 });
        setModalIsOpen(true);
    };

    const openModalForEdit = (vehicle) => {
        setIsEditing(true);
        setEditVehicleId(vehicle.vehicleId);
        setCurrentVehicle({
            licensePlate: vehicle.licensePlate,
            vehicleType: vehicle.vehicleType,
            totalSeats: vehicle.totalSeats
        });
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
        setError('');
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        const val = name === 'totalSeats' ? parseInt(value, 10) || 0 : value;
        setCurrentVehicle(prevState => ({
            ...prevState,
            [name]: val
        }));
    };

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError('');
         try {
             const payload = {
                ...currentVehicle,
                totalSeats: parseInt(currentVehicle.totalSeats, 10) // Ensure it's a number
            };

            if (isEditing) {
                await api.put(`/admin/vehicles/${editVehicleId}`, payload);
            } else {
                await api.post('/admin/vehicles', payload);
            }
            fetchVehicles();
            closeModal();
        } catch (err) {
            console.error("Lỗi khi lưu:", err);
            setError("Lỗi khi lưu thông tin xe. Vui lòng kiểm tra lại.");
        }
    };

    const openConfirmDeleteDialog = (vehicleId) => {
        setVehicleToDelete(vehicleId);
        setConfirmDeleteDialogOpen(true);
    };

    const closeConfirmDeleteDialog = () => {
        setVehicleToDelete(null);
        setConfirmDeleteDialogOpen(false);
    };

     const handleDeleteConfirm = async () => {
        if (vehicleToDelete) {
            try {
                setError('');
                await api.delete(`/admin/vehicles/${vehicleToDelete}`);
                fetchVehicles();
                closeConfirmDeleteDialog();
            } catch (err) {
                console.error("Lỗi khi xóa:", err);
                 // Check if the error is due to foreign key constraint
                 if (err.response && err.response.data && typeof err.response.data === 'string' && err.response.data.includes('violates foreign key constraint')) {
                     setError("Lỗi khi xóa xe: Xe này đang được sử dụng trong một chuyến đi.");
                 } else {
                    setError("Lỗi khi xóa xe.");
                 }
                // Keep the dialog open to show the error
                // closeConfirmDeleteDialog();
            }
        }
    };

    return (
        <Paper sx={{ p: 2, margin: 'auto', maxWidth: 1000, flexGrow: 1 }}>
            <Typography variant="h6" gutterBottom component="div">
                Quản lý Xe
            </Typography>

            <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openModalForCreate}
                sx={{ mb: 2 }}
            >
                Thêm Xe mới
            </Button>

            {error && !modalIsOpen && !confirmDeleteDialogOpen && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="vehicles table">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Biển số</TableCell>
                            <TableCell>Loại xe</TableCell>
                            <TableCell>Số ghế</TableCell>
                            <TableCell align="right">Hành động</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {vehicles.map((vehicle) => (
                            <TableRow key={vehicle.vehicleId}>
                                <TableCell>{vehicle.vehicleId}</TableCell>
                                <TableCell>{vehicle.licensePlate}</TableCell>
                                <TableCell>{vehicle.vehicleType}</TableCell>
                                <TableCell>{vehicle.totalSeats}</TableCell>
                                <TableCell align="right">
                                    <IconButton color="primary" onClick={() => openModalForEdit(vehicle)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton color="error" onClick={() => openConfirmDeleteDialog(vehicle.vehicleId)}>
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Edit/Create Dialog */}
            <Dialog open={modalIsOpen} onClose={closeModal}>
                <DialogTitle>{isEditing ? "Sửa thông tin Xe" : "Thêm Xe mới"}</DialogTitle>
                <DialogContent>
                    {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <TextField
                        autoFocus
                        margin="dense"
                        name="licensePlate"
                        label="Biển số"
                        type="text"
                        fullWidth
                        variant="standard"
                        value={currentVehicle.licensePlate}
                        onChange={handleInputChange}
                        required
                    />
                    <TextField
                        margin="dense"
                        name="vehicleType"
                        label="Loại xe"
                        type="text"
                        fullWidth
                        variant="standard"
                        value={currentVehicle.vehicleType}
                        onChange={handleInputChange}
                        required
                    />
                    <TextField
                        margin="dense"
                        name="totalSeats"
                        label="Số ghế"
                        type="number"
                        fullWidth
                        variant="standard"
                        value={currentVehicle.totalSeats}
                        onChange={handleInputChange}
                        required
                        inputProps={{ min: 1 }}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeModal}>Hủy</Button>
                    <Button onClick={handleFormSubmit} variant="contained">
                        {isEditing ? 'Lưu thay đổi' : 'Thêm mới'}
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
                        Bạn có chắc chắn muốn xóa xe ID {vehicleToDelete}? Hành động này không thể hoàn tác.
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

export default AdminVehiclesPage;