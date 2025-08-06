-- -----------------------------------------------------
-- Tính tiền công 1 ca làm việc
-- -----------------------------------------------------

DELIMITER $$

CREATE FUNCTION fn_TinhTienCongMotCaLamViec (
    p_MaNV VARCHAR(10),
    p_MaCaLamViec VARCHAR(10)
)
RETURNS DECIMAL(10,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_MaChucVu VARCHAR(10);
    DECLARE v_LuongTheoGio DECIMAL(10,2);
    DECLARE v_HeSoLuong DECIMAL(5,2);
    DECLARE v_ThoiGianVaoLam TIME;
    DECLARE v_ThoiGianKetThuc TIME;
    DECLARE v_SoGioLam DECIMAL(5,2);
    DECLARE v_TienCong DECIMAL(10,2);

    -- Lấy MaChucVu của nhân viên từ bảng NhanVien
    SELECT MaChucVu INTO v_MaChucVu FROM NhanVien WHERE MaNV = p_MaNV;

    -- Nếu không tìm thấy nhân viên, trả về 0 hoặc xử lý lỗi theo cách bạn muốn
    IF v_MaChucVu IS NULL THEN
        RETURN 0.00;
    END IF;

    -- Lấy LuongTheoGio của chức vụ từ bảng ChucVu
    SELECT LuongTheoGio INTO v_LuongTheoGio FROM ChucVu WHERE MaChucVu = v_MaChucVu;

    -- Nếu không tìm thấy chức vụ hoặc lương theo giờ, trả về 0
    IF v_LuongTheoGio IS NULL THEN
        RETURN 0.00;
    END IF;

    -- Lấy thông tin HeSoLuong, ThoiGianVaoLam, ThoiGianKetThuc từ bảng CaLamViec
    SELECT HeSoLuong, ThoiGianVaoLam, ThoiGianKetThuc
    INTO v_HeSoLuong, v_ThoiGianVaoLam, v_ThoiGianKetThuc
    FROM CaLamViec WHERE MaCaLamViec = p_MaCaLamViec;

    -- Nếu không tìm thấy ca làm việc hoặc thông tin không đầy đủ, trả về 0
    IF v_HeSoLuong IS NULL OR v_ThoiGianVaoLam IS NULL OR v_ThoiGianKetThuc IS NULL THEN
        RETURN 0.00;
    END IF;

    -- Tính số giờ làm việc của ca theo quy định
    -- Xử lý trường hợp ca qua đêm (ThoiGianKetThuc < ThoiGianVaoLam)
    IF v_ThoiGianKetThuc >= v_ThoiGianVaoLam THEN
        SET v_SoGioLam = TIME_TO_SEC(TIMEDIFF(v_ThoiGianKetThuc, v_ThoiGianVaoLam)) / 3600.0;
    ELSE
        -- Ca qua đêm: (số giây từ ThoiGianVaoLam đến nửa đêm) + (số giây từ đầu ngày đến ThoiGianKetThuc)
        SET v_SoGioLam = (TIME_TO_SEC(TIMEDIFF('24:00:00', v_ThoiGianVaoLam)) + TIME_TO_SEC(v_ThoiGianKetThuc)) / 3600.0;
    END IF;

    -- Tính tiền công cho một lần làm ca này
    SET v_TienCong = v_SoGioLam * v_LuongTheoGio * v_HeSoLuong;

    RETURN v_TienCong;

END$$

DELIMITER ;
 -- -----------------------------------------------------
-- Tính tổng chi phí hóa đơn dịch vụ
-- -----------------------------------------------------

DELIMITER $$

CREATE FUNCTION TinhTongChiPhiHoaDonDV (
    p_MaHD VARCHAR(15),
    p_ApDungUuDai BOOLEAN
)
RETURNS DECIMAL(10, 2)
DETERMINISTIC
BEGIN
    DECLARE v_TongChiPhi DECIMAL(10, 2);
    DECLARE v_MucUuDai DECIMAL(5, 2);

    -- Tính tổng chi phí dịch vụ
    SELECT SUM(dv.DonGia * cthd.SoLuong)
    INTO v_TongChiPhi
    FROM CT_HoaDonDV cthd
    JOIN DichVu dv ON cthd.MaDV = dv.MaDV
    WHERE cthd.MaHD = p_MaHD;

    -- Áp dụng ưu đãi nếu được yêu cầu
    IF p_ApDungUuDai THEN
        SELECT ud.MucUuDai
        INTO v_MucUuDai
        FROM HoaDonDV hd
        JOIN UuDai ud ON hd.MaUuDai = ud.MaUuDai
        WHERE hd.MaHD = p_MaHD;

        -- Giảm chi phí theo phần trăm ưu đãi
        SET v_TongChiPhi = v_TongChiPhi * (1 - v_MucUuDai / 100);
    END IF;

    -- Làm tròn chi phí
    RETURN ROUND(v_TongChiPhi, 2);
END $$

DELIMITER ;


DELIMITER $$
-- -----------------------------------------------------
-- Tính chi phí phiên sử dụng
-- -----------------------------------------------------


CREATE FUNCTION TinhChiPhiPhienSuDung (
    p_MaPhien INT UNSIGNED,
    p_ApDungUuDai BOOLEAN
)
RETURNS DECIMAL(10, 2)
DETERMINISTIC
BEGIN
    DECLARE v_ThoiGianBatDau DATETIME;
    DECLARE v_ThoiGianKetThuc DATETIME;
    DECLARE v_GiaTheoGio DECIMAL(10, 2);
    DECLARE v_MucUuDai DECIMAL(5, 2);
    DECLARE v_ChiPhi DECIMAL(10, 2);
    DECLARE v_SoGioSuDung DECIMAL(10, 2);

    -- Lấy thông tin phiên sử dụng
    SELECT ThoiGianBatDau, ThoiGianKetThuc
    INTO v_ThoiGianBatDau, v_ThoiGianKetThuc
    FROM PhienSuDung
    WHERE MaPhien = p_MaPhien;

    -- Nếu phiên chưa kết thúc, sử dụng thời điểm hiện tại
    IF v_ThoiGianKetThuc IS NULL THEN
        SET v_ThoiGianKetThuc = NOW();
    END IF;

    -- Tính số giờ sử dụng (tính bằng giờ, bao gồm phần lẻ)
    SET v_SoGioSuDung = TIMESTAMPDIFF(MINUTE, v_ThoiGianBatDau, v_ThoiGianKetThuc) / 60.0;

    -- Lấy giá theo giờ của loại máy
    SELECT lm.GiaTheoGio
    INTO v_GiaTheoGio
    FROM PhienSuDung ps
    JOIN MayTinh mt ON ps.MaMay = mt.MaMay
    JOIN LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
    WHERE ps.MaPhien = p_MaPhien;

    -- Tính chi phí cơ bản
    SET v_ChiPhi = v_SoGioSuDung * v_GiaTheoGio;

    -- Áp dụng ưu đãi nếu được yêu cầu
    IF p_ApDungUuDai THEN
        SELECT ud.MucUuDai
        INTO v_MucUuDai
        FROM PhienSuDung ps
        JOIN TaiKhoan tk ON ps.MaTK = tk.MaTK
        JOIN KhachHang kh ON tk.MaKH = kh.MaKH
        JOIN LoaiKH lkh ON kh.MaLoaiKH = lkh.MaLoaiKH
        JOIN UuDai ud ON lkh.MaUuDai = ud.MaUuDai
        WHERE ps.MaPhien = p_MaPhien;

        -- Giảm chi phí theo phần trăm ưu đãi
        SET v_ChiPhi = v_ChiPhi * (1 - v_MucUuDai / 100);
    END IF;

    -- Làm tròn chi phí đến 2 chữ số thập phân
    RETURN ROUND(v_ChiPhi, 2);
END $$

DELIMITER ;

