import React, { useEffect, useState } from 'react';
import api from '../api';
import Modal from 'react-modal'; // <-- 1. Import Modal

// --- 2. Cấu hình Modal (cần thiết) ---
Modal.setAppElement('#root'); // '#root' là ID của div gốc trong public/index.html

function AdminRoutesPage() {
    const [routes, setRoutes] = useState([]);
    const [error, setError] = useState('');
    
    // --- 3. State cho Modal ---
    const [modalIsOpen, setModalIsOpen] = useState(false);
    const [currentRoute, setCurrentRoute] = useState({ // Dữ liệu trong form modal
        startLocation: '',
        endLocation: '',
        distanceKm: 0
    });
    const [isEditing, setIsEditing] = useState(false); // Đang sửa hay tạo mới?
    const [editRouteId, setEditRouteId] = useState(null); // ID của route đang sửa

    // --- 4. Hàm fetch dữ liệu (giữ nguyên) ---
    useEffect(() => {
        fetchRoutes(); // Tách ra thành hàm riêng để gọi lại sau khi thêm/sửa/xóa
    }, []);

    const fetchRoutes = async () => {
        try {
            setError(''); // Xóa lỗi cũ
            const response = await api.get('/admin/routes');
            setRoutes(response.data);
        } catch (err) {
            console.error("Lỗi khi tải tuyến đường:", err);
            setError("Không thể tải dữ liệu tuyến đường.");
        }
    };

    // --- 5. Hàm xử lý Modal ---
    const openModalForCreate = () => {
        setIsEditing(false);
        setCurrentRoute({ startLocation: '', endLocation: '', distanceKm: 0 }); // Reset form
        setModalIsOpen(true);
    };

    const openModalForEdit = (route) => {
        setIsEditing(true);
        setEditRouteId(route.routeId);
        setCurrentRoute({ // Điền dữ liệu cũ vào form
            startLocation: route.startLocation,
            endLocation: route.endLocation,
            distanceKm: route.distanceKm
        });
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
        setError(''); // Xóa lỗi modal nếu có
    };

    // --- 6. Hàm xử lý Form trong Modal ---
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCurrentRoute(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    // --- 7. Hàm gọi API Tạo/Sửa ---
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isEditing) {
                // Gọi API PUT để sửa
                await api.put(`/admin/routes/${editRouteId}`, currentRoute);
            } else {
                // Gọi API POST để tạo mới
                await api.post('/admin/routes', currentRoute);
            }
            fetchRoutes(); // Tải lại danh sách
            closeModal(); // Đóng modal
        } catch (err) {
            console.error("Lỗi khi lưu:", err);
            setError("Lỗi khi lưu tuyến đường. Vui lòng kiểm tra lại dữ liệu.");
        }
    };

    // --- 8. Hàm gọi API Xóa ---
    const handleDelete = async (routeId) => {
        // Hỏi xác nhận trước khi xóa
        if (window.confirm(`Bạn có chắc muốn xóa tuyến đường ID ${routeId} không?`)) {
            try {
                await api.delete(`/admin/routes/${routeId}`);
                fetchRoutes(); // Tải lại danh sách
            } catch (err) {
                console.error("Lỗi khi xóa:", err);
                setError("Lỗi khi xóa tuyến đường.");
            }
        }
    };

    // --- 9. Phần JSX (Giao diện) ---
    return (
        <div>
            <h2>Quản lý Tuyến đường</h2>
            {/* Nút Tạo mới */}
            <button onClick={openModalForCreate}>Tạo Tuyến đường mới</button>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            {/* Bảng hiển thị dữ liệu */}
            <table>
                {/* ... (thead giữ nguyên) ... */}
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Điểm đi</th>
                        <th>Điểm đến</th>
                        <th>Khoảng cách (km)</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    {routes.map(route => (
                        <tr key={route.routeId}>
                            <td>{route.routeId}</td>
                            <td>{route.startLocation}</td>
                            <td>{route.endLocation}</td>
                            <td>{route.distanceKm}</td>
                            <td>
                                {/* Nút Sửa và Xóa */}
                                <button onClick={() => openModalForEdit(route)}>Sửa</button>
                                <button onClick={() => handleDelete(route.routeId)}>Xóa</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* --- Modal Tạo/Sửa --- */}
            <Modal
                isOpen={modalIsOpen}
                onRequestClose={closeModal}
                contentLabel={isEditing ? "Sửa Tuyến đường" : "Tạo Tuyến đường mới"}
                // (Thêm style cho modal nếu muốn)
                style={{
                    content: { top: '50%', left: '50%', right: 'auto', bottom: 'auto', marginRight: '-50%', transform: 'translate(-50%, -50%)', width: '400px' }
                }}
            >
                <h2>{isEditing ? "Sửa Tuyến đường" : "Tạo Tuyến đường mới"}</h2>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <form onSubmit={handleFormSubmit}>
                    <label>
                        Điểm đi:
                        <input
                            type="text"
                            name="startLocation"
                            value={currentRoute.startLocation}
                            onChange={handleInputChange}
                            required
                        />
                    </label>
                    <br />
                    <label>
                        Điểm đến:
                        <input
                            type="text"
                            name="endLocation"
                            value={currentRoute.endLocation}
                            onChange={handleInputChange}
                            required
                        />
                    </label>
                    <br />
                    <label>
                        Khoảng cách (km):
                        <input
                            type="number"
                            name="distanceKm"
                            value={currentRoute.distanceKm}
                            onChange={handleInputChange}
                            required
                            min="0"
                        />
                    </label>
                    <br /><br />
                    <button type="submit">{isEditing ? 'Lưu thay đổi' : 'Tạo mới'}</button>
                    <button type="button" onClick={closeModal}>Hủy</button>
                </form>
            </Modal>
        </div>
    );
}

export default AdminRoutesPage;