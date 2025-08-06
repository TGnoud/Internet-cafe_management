use quan_ly_quan_net;

-- Máy 'MAY99' không tồn tại
INSERT INTO PhienSuDung (MaMay, MaTK, ThoiGianBatDau)
VALUES ('MAY99', 'TK001', NOW());

-- MAY02 đang có TrangThai = 'Đang sử dụng'
INSERT INTO MayTinh (MaMay, TenMay, TrangThai, MaLoaiMay) VALUES
('MAY01', 'Máy 01', 'Khả dụng', 'LMAY01'),
('MAY02', 'Máy 02', 'Đang sử dụng', 'LMAY01'),
('MAY03', 'Máy 03', 'Bảo trì', 'LMAY02');

INSERT INTO PhienSuDung (MaMay, MaTK, ThoiGianBatDau)
VALUES ('MAY02', 'TK001', NOW());

-- TK999 không tồn tại
INSERT INTO PhienSuDung (MaMay, MaTK, ThoiGianBatDau)
VALUES ('MAY01', 'TK0999', NOW());

-- TK02 có SoTienConLai = 0.00
INSERT IGNORE INTO KhachHang (MaKH, HoTen, SoDienThoai, GioiTinh, MaLoaiKH) VALUES
('KH0999', 'Nguyễn Văn A', '0901234567', 'Nam', 'LKH01');

INSERT INTO TaiKhoan (MaTK, TenTK, MatKhau, SoTienConLai, MaKH) VALUES
('TK0999', 'userB', 'hashed_password_B', 0.00, 'KH0999');

INSERT INTO PhienSuDung (MaMay, MaTK, ThoiGianBatDau)
VALUES ('PC50', 'TK0999', NOW());

-- 2. Chuyển trạng thái
INSERT INTO PhienSuDung (MaMay, MaTK, ThoiGianBatDau)
VALUES ('PC50', 'TK001', NOW());

SELECT TrangThai FROM MayTinh WHERE MaMay = 'MAY01';

UPDATE PhienSuDung
SET ThoiGianKetThuc = NOW()
WHERE MaMay = 'PC50' AND MaTK = 'TK001' AND ThoiGianKetThuc IS NULL;

-- Kiểm tra trạng thái máy 
SELECT TrangThai FROM MayTinh WHERE MaMay = 'PC50';
-- Kiểm tra số tiền còn lại
SELECT SoTienConLai FROM TaiKhoan WHERE MaTK = 'TK001';


-- 4. Trừ tiền
INSERT INTO CT_HoaDonDV (MaHD, MaDV, SoLuong) VALUES
('HD001', 'DV011', 2),
('HD001', 'DV012', 1);  

-- Tạo hóa đơn
INSERT INTO HoaDonDV (MaHD, ThoiDiemThanhToan, MaTK, MaNV, MaUuDai)
VALUES ('HD0999', NOW(), 'TK001', 'NV001', NULL);

-- Kiểm tra số tiền còn lại của TK001 
SELECT SoTienConLai FROM TaiKhoan WHERE MaTK = 'TK001';



