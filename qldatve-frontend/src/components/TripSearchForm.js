import React, { useState } from 'react';
// 1. DỌN DẸP IMPORT: Gộp các import MUI lại
import {
    Box, 
    TextField, 
    Button, 
    Grid, 
    FormControl, 
    InputLabel, 
    Select, 
    MenuItem 
} from '@mui/material';
// 2. Import DatePicker của MUI
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { VIETNAM_PROVINCES } from '../data/provinces'; // Import danh sách tỉnh

function TripSearchForm({ onSearch }) {
    const [startLocation, setStartLocation] = useState(''); // State cho điểm đi
    const [endLocation, setEndLocation] = useState('');     // State cho điểm đến
    const [date, setDate] = useState(new Date()); // 3. Dùng state 'date' cho MUI DatePicker

    const handleSubmit = (e) => {
        e.preventDefault();
        
        // 4. Format 'date' (thay vì dateString)
        const formattedDate = date.toISOString().split('T')[0];
        
        onSearch({
            start_location: startLocation,
            end_location: endLocation,
            date: formattedDate // 5. Gửi 'formattedDate'
        });
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ 
            mt: 3, mb: 4, p: { xs: 2, sm: 3 }, // Thêm padding responsive
            backgroundColor: 'rgba(255, 255, 255, 0.95)', 
            borderRadius: 2, 
            boxShadow: 3 
        }}>
            <Grid container spacing={2} alignItems="center">

                {/* Dropdown Điểm đi (Giữ nguyên, đã đúng) */}
                <Grid item xs={12} sm={4}>
                    <FormControl fullWidth size="small" required>
                        <InputLabel id="start-location-label">Điểm đi</InputLabel>
                        <Select
                            labelId="start-location-label"
                            label="Điểm đi"
                            value={startLocation}
                            onChange={(e) => setStartLocation(e.target.value)}
                        >
                            <MenuItem value=""><em>-- Chọn điểm đi --</em></MenuItem>
                            {VIETNAM_PROVINCES.map((province) => (
                                <MenuItem key={province} value={province}>
                                    {province}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Grid>

                {/* Dropdown Điểm đến (Giữ nguyên, đã đúng) */}
                <Grid item xs={12} sm={4}>
                     <FormControl fullWidth size="small" required>
                        <InputLabel id="end-location-label">Điểm đến</InputLabel>
                        <Select
                            labelId="end-location-label"
                            label="Điểm đến"
                            value={endLocation}
                            onChange={(e) => setEndLocation(e.target.value)}
                        >
                            <MenuItem value=""><em>-- Chọn điểm đến --</em></MenuItem>
                            {VIETNAM_PROVINCES.map((province) => (
                                <MenuItem key={province} value={province}>
                                    {province}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Grid>

                {/* 6. SỬ DỤNG MUI DatePicker (Đồng bộ) */}
                <Grid item xs={12} sm={2}>
                    <FormControl fullWidth size="small" required>
                        <DatePicker
                            label="Ngày đi"
                            value={date}
                            onChange={(newDate) => setDate(newDate || new Date())}
                            minDate={new Date()} // Không cho chọn ngày quá khứ
                            format="dd/MM/yyyy" // Định dạng hiển thị
                            // Dùng TextField của MUI làm input
                            slots={{
                                textField: (params) => <TextField {...params} size="small" required />
                            }}
                        />
                    </FormControl>
                </Grid>
                {/* --------------------------- */}

                {/* Nút Tìm kiếm (Giữ nguyên) */}
                <Grid item xs={12} sm={2}>
                    <Button type="submit" variant="contained" fullWidth>
                        Tìm kiếm
                    </Button>
                </Grid>
            </Grid>
        </Box>
    );
}

export default TripSearchForm;