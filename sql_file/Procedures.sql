-- -----------------------------------------------------
-- Kiểm tra trạng thái máy
-- -----------------------------------------------------


DELIMITER $$

CREATE PROCEDURE KiemTraTrangThaiMay (
    IN p_MaMay VARCHAR(10),
    OUT p_TrangThai VARCHAR(45)
)
BEGIN
    DECLARE v_DangSuDung INT;

    -- Lấy trạng thái từ bảng MayTinh
    SELECT TrangThai
    INTO p_TrangThai
    FROM MayTinh
    WHERE MaMay = p_MaMay;

    -- Kiểm tra xem máy có đang được sử dụng (phiên chưa kết thúc)
    SELECT COUNT(*)
    INTO v_DangSuDung
    FROM PhienSuDung
    WHERE MaMay = p_MaMay AND ThoiGianKetThuc IS NULL;

    IF v_DangSuDung > 0 THEN
        SET p_TrangThai = 'Đang sử dụng';
    END IF;
END $$
DELIMITER ;

CALL KiemTraTrangThaiMay('PC01', @TrangThai);
SELECT @TrangThai AS TrangThai;

