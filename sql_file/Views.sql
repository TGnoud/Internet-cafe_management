-- Đảm bảo bạn đang sử dụng đúng cơ sở dữ liệu
USE quan_ly_quan_net;

-- -----------------------------------------------------
-- VIEWS CHO KHÁCH HÀNG
-- -----------------------------------------------------

-- 1. View_NV_TrangThaiMay
CREATE VIEW View_NV_TrangThaiMay AS
SELECT
    mt.MaMay,
    mt.TenMay,
    mt.TrangThai,
    lm.MoTa AS LoaiMayMoTa,
    lm.GiaTheoGio,
    psd_active.MaTK AS MaTKDangSuDung,
    tk_active.TenTK AS TenTKDangSuDung,
    psd_active.ThoiGianBatDau AS ThoiGianBatDauPhienHienTai
FROM
    MayTinh mt
JOIN
    LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
LEFT JOIN
    PhienSuDung psd_active ON mt.MaMay = psd_active.MaMay AND psd_active.ThoiGianKetThuc IS NULL
LEFT JOIN
    TaiKhoan tk_active ON psd_active.MaTK = tk_active.MaTK;

-- 2. View_KH_ThongTinKhachHangCoBan
CREATE VIEW View_KH_ThongTinKhachHangCoBan AS
SELECT
    kh.MaKH,
    kh.HoTen,
    kh.SoDienThoai,
    tk.MaTK,
    tk.TenTK,
    tk.SoTienConLai,
    lkh.TenLoai AS TenLoaiKhachHang
FROM
    KhachHang kh
JOIN
    TaiKhoan tk ON kh.MaKH = tk.MaKH
JOIN
    LoaiKH lkh ON kh.MaLoaiKH = lkh.MaLoaiKH;
    
-- -----------------------------------------------------
-- VIEWS CHO NHÂN VIÊN
-- -----------------------------------------------------

-- 1. View_NV_ThongTinCaNhan
CREATE VIEW View_NV_ThongTinCaNhan AS
SELECT
    kh.MaKH,
    kh.HoTen,
    kh.SoDienThoai,
    kh.GioiTinh,
    tk.MaTK,
    tk.TenTK,
    tk.SoTienConLai,
    lkh.TenLoai AS TenLoaiKhachHang,
    ud.NoiDung AS NoiDungUuDaiLoaiKH,
    ud.MucUuDai AS PhanTramUuDaiLoaiKH
FROM
    KhachHang kh
JOIN
    TaiKhoan tk ON kh.MaKH = tk.MaKH
JOIN
    LoaiKH lkh ON kh.MaLoaiKH = lkh.MaLoaiKH
LEFT JOIN
    UuDai ud ON lkh.MaUuDai = ud.MaUuDai;
    
-- 2. View_NV_LichSuSuDungMay
CREATE VIEW View_NV_LichSuSuDungMay AS
SELECT
    psd.MaPhien,
    tk.MaTK,
    kh.HoTen AS TenKhachHang,
    mt.MaMay,
    mt.TenMay,
    lm.MoTa AS LoaiMayMoTa,
    lm.GiaTheoGio,
    psd.ThoiGianBatDau,
    psd.ThoiGianKetThuc,
    TIMESTAMPDIFF(SECOND, psd.ThoiGianBatDau, psd.ThoiGianKetThuc) AS ThoiGianSuDungGiay,
    ROUND(
        (TIMESTAMPDIFF(SECOND, psd.ThoiGianBatDau, psd.ThoiGianKetThuc) / 3600.0) * lm.GiaTheoGio,
    2) AS ChiPhiPhienDuKien
FROM
    PhienSuDung psd
JOIN
    TaiKhoan tk ON psd.MaTK = tk.MaTK
JOIN
    KhachHang kh ON tk.MaKH = kh.MaKH
JOIN
    MayTinh mt ON psd.MaMay = mt.MaMay
JOIN
    LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
WHERE
    psd.ThoiGianKetThuc IS NOT NULL; 
    
-- 3. View_NV_LichSuMuaDichVu
CREATE VIEW View_NV_LichSuMuaDichVu AS
SELECT
    hddv.MaHD,
    tk.MaTK,
    kh.HoTen AS TenKhachHang,
    hddv.ThoiDiemThanhToan,
    dv.MaDV,
    dv.TenDV,
    cthd.SoLuong,
    dv.DonGia AS DonGiaDichVu,
    (cthd.SoLuong * dv.DonGia) AS ThanhTienChiTiet,
    ud_hd.NoiDung AS NoiDungUuDaiHoaDon,
    ud_hd.MucUuDai AS PhanTramUuDaiHoaDon
FROM
    HoaDonDV hddv
JOIN
    TaiKhoan tk ON hddv.MaTK = tk.MaTK
JOIN
    KhachHang kh ON tk.MaKH = kh.MaKH
JOIN
    CT_HoaDonDV cthd ON hddv.MaHD = cthd.MaHD
JOIN
    DichVu dv ON cthd.MaDV = dv.MaDV
LEFT JOIN
    UuDai ud_hd ON hddv.MaUuDai = ud_hd.MaUuDai;

-- 4. View_KH_DanhSachMayVaTrangThai
CREATE VIEW View_kh_DanhSachMayVaTrangThai AS
SELECT
    mt.MaMay,
    mt.TenMay,
    mt.TrangThai,
    lm.MoTa AS LoaiMayMoTa,
    lm.GiaTheoGio
FROM
    MayTinh mt
JOIN
    LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay;


-- 3. View_KH_DanhSachDichVu
CREATE VIEW View_kh_DanhSachDichVu AS
SELECT
    MaDV,
    TenDV,
    DonGia,
    TrangThaiDichVu
FROM
    DichVu;

-- 4. View_NV_PhienDangHoatDongChiTiet
CREATE VIEW View_NV_PhienDangHoatDongChiTiet AS
SELECT
    psd.MaPhien,
    psd.MaMay,
    mt.TenMay,
    tk.MaTK,
    tk.TenTK AS TenTaiKhoanKhach,
    kh.HoTen AS TenKhachHang,
    psd.ThoiGianBatDau,
    lm.MoTa AS LoaiMayMoTa,
    lm.GiaTheoGio,
    TIMESTAMPDIFF(MINUTE, psd.ThoiGianBatDau, NOW()) AS SoPhutDaSuDung,
    ROUND(
        (TIMESTAMPDIFF(MINUTE, psd.ThoiGianBatDau, NOW()) / 60.0) * lm.GiaTheoGio,
    2) AS ChiPhiTamTinh
FROM
    PhienSuDung psd
JOIN
    MayTinh mt ON psd.MaMay = mt.MaMay
JOIN
    LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
JOIN
    TaiKhoan tk ON psd.MaTK = tk.MaTK
JOIN
    KhachHang kh ON tk.MaKH = kh.MaKH
WHERE
    psd.ThoiGianKetThuc IS NULL; -- Chỉ lấy các phiên đang hoạt động

-- 5. View_NV_LichLamViecNhanVien
CREATE VIEW View_NV_LichLamViecNhanVien AS
SELECT
    nv.MaNV,
    nv.HoTen AS TenNhanVien,
    cv.TenChucVu,
    clv.MaCaLamViec,
    DATE_FORMAT(clv.ThoiGianVaoLam, '%H:%i') AS ThoiGianVaoLam,
    DATE_FORMAT(clv.ThoiGianKetThuc, '%H:%i') AS ThoiGianKetThuc,
    clv.HeSoLuong
FROM
    NhanVien nv
JOIN
    NhanVien_CaLamViec nv_clv ON nv.MaNV = nv_clv.MaNV
JOIN
    CaLamViec clv ON nv_clv.MaCaLamViec = clv.MaCaLamViec
JOIN
    ChucVu cv ON nv.MaChucVu = cv.MaChucVu
ORDER BY
    nv.MaNV, clv.ThoiGianVaoLam;

-- -----------------------------------------------------
-- VIEWS CHO QUẢN LÝ
-- -----------------------------------------------------

-- 1. View_QL_DoanhThuTienGioChiTiet
CREATE VIEW View_QL_DoanhThuTienGioChiTiet AS
SELECT
    psd.MaPhien,
    psd.MaMay,
    mt.TenMay,
    lm.MoTa AS LoaiMayMoTa,
    psd.MaTK,
    kh.HoTen AS TenKhachHang,
    psd.ThoiGianBatDau,
    psd.ThoiGianKetThuc,
    TIMESTAMPDIFF(SECOND, psd.ThoiGianBatDau, psd.ThoiGianKetThuc) AS ThoiGianSuDungGiay,
    (TIMESTAMPDIFF(SECOND, psd.ThoiGianBatDau, psd.ThoiGianKetThuc) / 3600.0) AS ThoiGianSuDungGio,
    lm.GiaTheoGio,
    ROUND(
        (TIMESTAMPDIFF(SECOND, psd.ThoiGianBatDau, psd.ThoiGianKetThuc) / 3600.0) * lm.GiaTheoGio,
    2) AS DoanhThuPhien
FROM
    PhienSuDung psd
JOIN
    MayTinh mt ON psd.MaMay = mt.MaMay
JOIN
    LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
JOIN
    TaiKhoan tk ON psd.MaTK = tk.MaTK
JOIN
    KhachHang kh ON tk.MaKH = kh.MaKH
WHERE
    psd.ThoiGianKetThuc IS NOT NULL;

-- 2. View_QL_ChiTietHoaDonDichVu
CREATE VIEW View_QL_ChiTietHoaDonDichVu AS
SELECT
    hddv.MaHD,
    hddv.ThoiDiemThanhToan,
    tk.MaTK,
    kh.HoTen AS TenKhachHang,
    nv.MaNV AS MaNhanVienLapHD,
    nv.HoTen AS TenNhanVienLapHD,
    dv.MaDV,
    dv.TenDV,
    cthd.SoLuong,
    dv.DonGia AS DonGiaDichVu,
    (cthd.SoLuong * dv.DonGia) AS ThanhTienChiTietDV,
    ud_hd.MaUuDai AS MaUuDaiHoaDon,
    ud_hd.NoiDung AS NoiDungUuDaiHoaDon,
    ud_hd.MucUuDai AS PhanTramUuDaiHoaDon
FROM
    HoaDonDV hddv
JOIN
    CT_HoaDonDV cthd ON hddv.MaHD = cthd.MaHD
JOIN
    DichVu dv ON cthd.MaDV = dv.MaDV
JOIN
    TaiKhoan tk ON hddv.MaTK = tk.MaTK
JOIN
    KhachHang kh ON tk.MaKH = kh.MaKH
LEFT JOIN
    NhanVien nv ON hddv.MaNV = nv.MaNV
LEFT JOIN
    UuDai ud_hd ON hddv.MaUuDai = ud_hd.MaUuDai;

-- 3. View_QL_ThongTinNhanVienDayDu
CREATE VIEW View_QL_ThongTinNhanVienDayDu AS
SELECT
    nv.MaNV,
    nv.HoTen,
    nv.SoDienThoai,
    nv.GioiTinh,
    nv.NgaySinh,
    cv.MaChucVu,
    cv.TenChucVu,
    cv.LuongTheoGio
FROM
    NhanVien nv
JOIN
    ChucVu cv ON nv.MaChucVu = cv.MaChucVu;

-- 4. View_QL_ThongKeTaiKhoanKhachHang
CREATE VIEW View_QL_ThongKeTaiKhoanKhachHang AS
SELECT
    kh.MaKH,
    kh.HoTen,
    kh.SoDienThoai,
    tk.MaTK,
    tk.TenTK,
    tk.SoTienConLai,
    lkh.TenLoai AS TenLoaiKhachHang,
    ud_lkh.NoiDung AS NoiDungUuDaiLoaiKH,
    ud_lkh.MucUuDai AS PhanTramUuDaiLoaiKH,
    (SELECT MAX(psd.ThoiGianBatDau) FROM PhienSuDung psd WHERE psd.MaTK = tk.MaTK) AS LanHoatDongCuoi
FROM
    KhachHang kh
JOIN
    TaiKhoan tk ON kh.MaKH = tk.MaKH
JOIN
    LoaiKH lkh ON kh.MaLoaiKH = lkh.MaLoaiKH
LEFT JOIN
    UuDai ud_lkh ON lkh.MaUuDai = ud_lkh.MaUuDai;
