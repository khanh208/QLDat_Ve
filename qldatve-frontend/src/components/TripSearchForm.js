import React, { useState } from 'react';
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

import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { VIETNAM_PROVINCES } from '../data/provinces';

function TripSearchForm({ onSearch }) {
  const [startLocation, setStartLocation] = useState('');
  const [endLocation, setEndLocation] = useState('');
  const [date, setDate] = useState(new Date());

  const handleSubmit = (e) => {
    e.preventDefault();

    // Guard: nếu date null thì dùng hôm nay
    const safeDate = date || new Date();
    const formattedDate = safeDate.toISOString().split('T')[0];

    onSearch({
      start_location: startLocation,
      end_location: endLocation,
      date: formattedDate
    });
  };

  return (
    <Box
      component="form"
      onSubmit={handleSubmit}
      sx={{
        mt: 3,
        mb: 4,
        p: { xs: 2, sm: 3 },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderRadius: 2,
        boxShadow: 3
      }}
    >
      <Grid container spacing={2} alignItems="center">
        {/* Dropdown Điểm đi */}
        <Grid item xs={12} sm={4}>
          <FormControl fullWidth size="small" required>
            <InputLabel id="start-location-label">Điểm đi</InputLabel>
            <Select
              labelId="start-location-label"
              label="Điểm đi"
              value={startLocation}
              onChange={(e) => setStartLocation(e.target.value)}
            >
              <MenuItem value="">
                <em>-- Chọn điểm đi --</em>
              </MenuItem>
              {VIETNAM_PROVINCES.map((province) => (
                <MenuItem key={province} value={province}>
                  {province}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        {/* Dropdown Điểm đến */}
        <Grid item xs={12} sm={4}>
          <FormControl fullWidth size="small" required>
            <InputLabel id="end-location-label">Điểm đến</InputLabel>
            <Select
              labelId="end-location-label"
              label="Điểm đến"
              value={endLocation}
              onChange={(e) => setEndLocation(e.target.value)}
            >
              <MenuItem value="">
                <em>-- Chọn điểm đến --</em>
              </MenuItem>
              {VIETNAM_PROVINCES.map((province) => (
                <MenuItem key={province} value={province}>
                  {province}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>

        {/* DatePicker */}
        <Grid item xs={12} sm={2}>
          <FormControl fullWidth size="small" required>
            <DatePicker
              label="Ngày đi"
              value={date}
              onChange={(newDate) => setDate(newDate || new Date())}
              minDate={new Date()}
              format="dd/MM/yyyy"
              // Fix lỗi sectionListRef / accessible field structure
              enableAccessibleFieldDOMStructure={false}
              // ✅ Dùng TextField component đúng chuẩn MUI X
              slots={{ textField: TextField }}
              // ✅ Truyền props cho TextField qua slotProps (không dùng function)
              slotProps={{
                textField: {
                  size: 'small',
                  required: true,
                  fullWidth: true
                }
              }}
            />
          </FormControl>
        </Grid>

        {/* Nút Tìm kiếm */}
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
