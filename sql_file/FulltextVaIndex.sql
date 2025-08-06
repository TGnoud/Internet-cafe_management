Use quan_ly_quan_net;

-- FULLTEXT index cho tên Khách Hàng
ALTER TABLE KhachHang
  ADD FULLTEXT INDEX ft_HoTen_KH (HoTen);
  
SHOW INDEX FROM KhachHang;
SELECT * FROM KhachHang
WHERE MATCH(HoTen) AGAINST ('+Minh' IN BOOLEAN MODE);

-- FULLTEXT index cho tên Tài Khoản
ALTER TABLE TaiKhoan
  ADD FULLTEXT INDEX ft_TenTK (TenTK);

-- FULLTEXT index cho tên Dịch Vụ
ALTER TABLE DichVu
  ADD FULLTEXT INDEX ft_TenDV (TenDV);

-- FULLTEXT index cho tên Nhân Viên
ALTER TABLE NhanVien
  ADD FULLTEXT INDEX ft_HoTen_NV (HoTen);
  
-- Chỉ tạo index nếu chưa có
CREATE INDEX idx_TaiKhoan_MaKH ON TaiKhoan (MaKH);
CREATE INDEX idx_PhienSuDung_MaTK ON PhienSuDung (MaTK);
CREATE INDEX idx_HoaDonDV_MaTK ON HoaDonDV (MaTK);

SHOW INDEX FROM taikhoan;

