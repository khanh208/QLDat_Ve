import React, { useEffect, useState } from 'react';
import api from '../api';
// import Modal from 'react-modal'; // <-- REMOVE THIS IMPORT

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
    Typography // For titles
} from '@mui/material';
import { Edit as EditIcon, Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';

// No need for Modal.setAppElement('#root'); anymore

function AdminRoutesPage() {
    const [routes, setRoutes] = useState([]);
    const [error, setError] = useState(''); // Used for displaying errors
    const [modalIsOpen, setModalIsOpen] = useState(false); // Used for Dialog open state
    const [currentRoute, setCurrentRoute] = useState({ startLocation: '', endLocation: '', distanceKm: 0 }); // Used for form data
    const [isEditing, setIsEditing] = useState(false); // Used to determine Dialog title/button text
    const [editRouteId, setEditRouteId] = useState(null); // Used in handleFormSubmit for PUT request
    const [confirmDeleteDialogOpen, setConfirmDeleteDialogOpen] = useState(false);
    const [routeToDelete, setRouteToDelete] = useState(null);

    // Fetch data on component mount
    useEffect(() => {
        fetchRoutes();
    }, []);

    // Function to fetch routes from API
    const fetchRoutes = async () => {
        try {
            setError('');
            const response = await api.get('/admin/routes');
            setRoutes(response.data); // Set state using setRoutes
        } catch (err) {
            console.error("Lỗi khi tải tuyến đường:", err);
            setError("Không thể tải dữ liệu tuyến đường.");
        }
    };

    // Functions to handle Dialog state
    const openModalForCreate = () => {
        setIsEditing(false); // Use setIsEditing
        setCurrentRoute({ startLocation: '', endLocation: '', distanceKm: 0 }); // Use setCurrentRoute
        setModalIsOpen(true); // Use setModalIsOpen
    };

    const openModalForEdit = (route) => {
        setIsEditing(true); // Use setIsEditing
        setEditRouteId(route.routeId); // Use setEditRouteId
        setCurrentRoute({
            startLocation: route.startLocation,
            endLocation: route.endLocation,
            distanceKm: route.distanceKm
        }); // Use setCurrentRoute
        setModalIsOpen(true); // Use setModalIsOpen
    };

    const closeModal = () => {
        setModalIsOpen(false); // Use setModalIsOpen
        setError('');
    };

    // Handle input changes in the form
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        // Ensure distanceKm is treated as a number
        const val = name === 'distanceKm' ? parseInt(value, 10) || 0 : value;
        setCurrentRoute(prevState => ({ // Use setCurrentRoute
            ...prevState,
            [name]: val
        }));
    };

    // Handle form submission (Create or Update)
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError(''); // Clear previous errors
        try {
            const payload = {
                ...currentRoute,
                distanceKm: parseInt(currentRoute.distanceKm, 10) // Ensure it's a number
            };

            if (isEditing) { // Use isEditing
                await api.put(`/admin/routes/${editRouteId}`, payload); // Use editRouteId
            } else {
                await api.post('/admin/routes', payload);
            }
            fetchRoutes();
            closeModal();
        } catch (err) {
            console.error("Lỗi khi lưu:", err);
            setError("Lỗi khi lưu tuyến đường. Vui lòng kiểm tra lại dữ liệu.");
            // Keep the modal open if there's an error
        }
    };

     // Handle opening the delete confirmation dialog
    const openConfirmDeleteDialog = (routeId) => {
        setRouteToDelete(routeId); // Use setRouteToDelete
        setConfirmDeleteDialogOpen(true); // Use setConfirmDeleteDialogOpen
    };

    // Handle closing the delete confirmation dialog
    const closeConfirmDeleteDialog = () => {
        setRouteToDelete(null); // Use setRouteToDelete
        setConfirmDeleteDialogOpen(false); // Use setConfirmDeleteDialogOpen
    };

    // Handle the actual deletion after confirmation
    const handleDeleteConfirm = async () => {
        if (routeToDelete) { // Use routeToDelete
            try {
                setError('');
                await api.delete(`/admin/routes/${routeToDelete}`); // Use routeToDelete
                fetchRoutes();
                closeConfirmDeleteDialog();
            } catch (err) {
                console.error("Lỗi khi xóa:", err);
                setError("Lỗi khi xóa tuyến đường.");
                // Keep dialog open to show error? Or close it.
                // closeConfirmDeleteDialog();
            }
        }
    };


    return (
        <Paper sx={{ p: 2, margin: 'auto', maxWidth: 1000, flexGrow: 1 }}>
            <Typography variant="h6" gutterBottom component="div">
                Quản lý Tuyến đường
            </Typography>

            <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={openModalForCreate}
                sx={{ mb: 2 }}
            >
                Tạo Tuyến đường mới
            </Button>

            {/* Display general errors here */}
            {error && !modalIsOpen && !confirmDeleteDialogOpen && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="routes table">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Điểm đi</TableCell>
                            <TableCell>Điểm đến</TableCell>
                            <TableCell>Khoảng cách (km)</TableCell>
                            <TableCell align="right">Hành động</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {routes.map((route) => (
                            <TableRow key={route.routeId}>
                                <TableCell>{route.routeId}</TableCell>
                                <TableCell>{route.startLocation}</TableCell>
                                <TableCell>{route.endLocation}</TableCell>
                                <TableCell>{route.distanceKm}</TableCell>
                                <TableCell align="right">
                                    <IconButton color="primary" onClick={() => openModalForEdit(route)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton color="error" onClick={() => openConfirmDeleteDialog(route.routeId)}>
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
                <DialogTitle>{isEditing ? "Sửa Tuyến đường" : "Tạo Tuyến đường mới"}</DialogTitle>
                <DialogContent>
                    {/* Display errors specific to the dialog */}
                    {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <TextField
                        autoFocus
                        margin="dense"
                        name="startLocation"
                        label="Điểm đi"
                        type="text"
                        fullWidth
                        variant="standard"
                        value={currentRoute.startLocation}
                        onChange={handleInputChange}
                        required
                    />
                    <TextField
                        margin="dense"
                        name="endLocation"
                        label="Điểm đến"
                        type="text"
                        fullWidth
                        variant="standard"
                        value={currentRoute.endLocation}
                        onChange={handleInputChange}
                        required
                    />
                    <TextField
                        margin="dense"
                        name="distanceKm"
                        label="Khoảng cách (km)"
                        type="number"
                        fullWidth
                        variant="standard"
                        value={currentRoute.distanceKm}
                        onChange={handleInputChange}
                        required
                        inputProps={{ min: 0 }}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeModal}>Hủy</Button>
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
                     {/* Display errors specific to delete */}
                     {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <DialogContentText>
                        Bạn có chắc chắn muốn xóa tuyến đường ID {routeToDelete}? Hành động này không thể hoàn tác.
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

export default AdminRoutesPage;