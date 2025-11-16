import React, { useEffect, useState } from 'react';
import api from '../api';
// --- IMPORT MUI COMPONENTS ---
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    IconButton,
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText, // <-- ADD THIS IMPORT HERE
    DialogTitle,
    Button,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Switch,
    FormControlLabel
} from '@mui/material';
import { Edit as EditIcon } from '@mui/icons-material'; // Chỉ cần icon Sửa
// ----------------------------

function AdminUsersPage() {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState('');

    // --- State cho Dialog Sửa ---
    const [modalIsOpen, setModalIsOpen] = useState(false);
    const [currentUser, setCurrentUser] = useState({ // Dữ liệu user đang sửa
        role: '',
        enabled: true
    });
    const [editUserId, setEditUserId] = useState(null);

    // --- Fetch Data ---
    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setError('');
            const response = await api.get('/admin/users');
            console.log("AdminUsersPage - API Response:", response.data);
            setUsers(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error("Lỗi khi tải danh sách người dùng:", err);
            setError(err.response?.data?.message || "Không thể tải dữ liệu người dùng.");
        }
    };

    // --- Dialog Handling ---
    const openModalForEdit = (user) => {
        setEditUserId(user.userId);
        setCurrentUser({ // Chỉ lấy role và enabled
            role: user.role,
            enabled: user.enabled
        });
        setError('');
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
        setError('');
    };

    // --- Form Input Handling ---
    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        // Xử lý riêng cho Switch (kiểu checkbox)
        const val = type === 'checkbox' ? checked : value;
        setCurrentUser(prevState => ({
            ...prevState,
            [name]: val
        }));
    };

    // --- API Call: Update ---
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!editUserId) return;

        try {
            // Payload chỉ chứa role và enabled
            const payload = {
                role: currentUser.role,
                enabled: currentUser.enabled
            };
            console.log("Submitting update payload:", payload);

            await api.put(`/admin/users/${editUserId}`, payload);
            fetchUsers(); // Tải lại danh sách
            closeModal(); // Đóng dialog
        } catch (err) {
            console.error("Lỗi khi cập nhật user:", err.response || err);
            setError(err.response?.data?.message || "Lỗi khi cập nhật người dùng.");
            // Giữ dialog mở để hiện lỗi
        }
    };

    // (Không có hàm Delete)

    console.log("AdminUsersPage - Rendering with users state:", users);

    return (
        <Paper sx={{ p: 2, margin: 'auto', maxWidth: 1000, flexGrow: 1 }}>
            <Typography variant="h6" gutterBottom component="div">
                Quản lý Người dùng (Admin)
            </Typography>

            {/* Hiển thị lỗi chung */}
            {error && !modalIsOpen && <Typography color="error" sx={{ mt: 2 }}>{error}</Typography>}

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="user table">
                    <TableHead>
                        <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Username</TableCell>
                            <TableCell>Email</TableCell>
                            <TableCell>Tên đầy đủ</TableCell>
                            <TableCell>SĐT</TableCell>
                            <TableCell>Role</TableCell>
                            <TableCell>Enabled</TableCell>
                            <TableCell align="right">Hành động</TableCell> {/* Chỉ có nút Sửa */}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {Array.isArray(users) && users.length > 0 ? (
                            users.map((user) => (
                                <TableRow
                                    key={user.userId}
                                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                >
                                    <TableCell>{user.userId}</TableCell>
                                    <TableCell>{user.username}</TableCell>
                                    <TableCell>{user.email}</TableCell>
                                    <TableCell>{user.fullName || 'N/A'}</TableCell>
                                    <TableCell>{user.phone || 'N/A'}</TableCell>
                                    <TableCell>{user.role}</TableCell>
                                    <TableCell>{user.enabled ? 'Yes' : 'No'}</TableCell>
                                    <TableCell align="right">
                                        <IconButton color="primary" onClick={() => openModalForEdit(user)}>
                                            <EditIcon />
                                        </IconButton>
                                        {/* Không có nút Xóa */}
                                    </TableCell>
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={8} align="center">Không có dữ liệu người dùng.</TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Edit Dialog */}
            <Dialog open={modalIsOpen} onClose={closeModal} fullWidth maxWidth="xs"> {/* Thu nhỏ Dialog */}
                <DialogTitle>Cập nhật Người dùng</DialogTitle>
                <DialogContent>
                    {error && <DialogContentText color="error">{error}</DialogContentText>}
                    <FormControl fullWidth margin="dense" required>
                        <InputLabel id="role-select-label">Quyền (Role)</InputLabel>
                        <Select
                            labelId="role-select-label"
                            label="Quyền (Role)"
                            name="role"
                            value={currentUser.role}
                            onChange={handleInputChange}
                        >
                            <MenuItem value="USER">USER</MenuItem>
                            <MenuItem value="ADMIN">ADMIN</MenuItem>
                        </Select>
                    </FormControl>

                    <FormControlLabel
                        control={
                            <Switch
                                name="enabled"
                                checked={currentUser.enabled}
                                onChange={handleInputChange}
                            />
                        }
                        label="Kích hoạt (Enabled)"
                        sx={{ mt: 1 }} // Thêm margin top
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeModal}>Hủy</Button>
                    <Button onClick={handleFormSubmit} variant="contained">
                        Lưu thay đổi
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Không có Dialog xác nhận xóa */}
        </Paper>
    );
}

export default AdminUsersPage;