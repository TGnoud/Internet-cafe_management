USE quan_ly_quan_net;

-- -----------------------------------------------------
-- TRIGGERS
-- -----------------------------------------------------

-- 1. Trigger kiểm tra điều kiện trước khi bắt đầu phiên sử dụng mới
DELIMITER $$
CREATE TRIGGER trg_Before_PhienSuDung_Insert_CheckPrerequisites
BEFORE INSERT ON PhienSuDung
FOR EACH ROW
BEGIN
    DECLARE var_TrangThaiMay VARCHAR(45);
    DECLARE var_SoTienConLai DECIMAL(10, 2);
    DECLARE var_TenMay VARCHAR(45);

    -- Kiểm tra trạng thái máy tính
    SELECT TenMay, TrangThai INTO var_TenMay, var_TrangThaiMay
    FROM MayTinh
    WHERE MaMay = NEW.MaMay;

    IF var_TrangThaiMay IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Loi: May tinh khong ton tai.';
    ELSEIF var_TrangThaiMay != 'Khả dụng' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Loi: May tinh hien khong co san. Vui long chon may khac.';
    END IF;
    -- Kiểm tra số tiền còn lại trong tài khoản
    SELECT SoTienConLai INTO var_SoTienConLai
    FROM TaiKhoan
    WHERE MaTK = NEW.MaTK;

    IF var_SoTienConLai IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Loi: Tai khoan voi khong ton tai.';
    ELSEIF var_SoTienConLai <= 0 THEN 
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Loi: Tai khoan khong du so du de bat dau phien su dung. Vui long nap them tien.';
    END IF;
END$$
DELIMITER ;

-- 2. Trigger cập nhật trạng thái máy tính thành 'Đang sử dụng' khi phiên bắt đầu
DELIMITER $$
CREATE TRIGGER trg_After_PhienSuDung_Insert_SetMayTinhBusy
AFTER INSERT ON PhienSuDung
FOR EACH ROW
BEGIN
    -- Chỉ cập nhật nếu phiên mới được tạo (ThoiGianKetThuc là NULL)
    IF NEW.ThoiGianKetThuc IS NULL THEN
        UPDATE MayTinh
        SET TrangThai = 'Đang sử dụng'
        WHERE MaMay = NEW.MaMay;
    END IF;
END$$
DELIMITER ;

-- 3. Trigger xử lý khi kết thúc phiên sử dụng
DELIMITER $$
CREATE TRIGGER trg_After_PhienSuDung_Update_FinalizeSession
AFTER UPDATE ON PhienSuDung
FOR EACH ROW
BEGIN
    DECLARE var_GiaTheoGio DECIMAL(10, 2);
    DECLARE var_duration_seconds INT;
    DECLARE var_cost DECIMAL(10, 2);

    -- Chỉ thực hiện khi ThoiGianKetThuc vừa được cập nhật (từ NULL sang một giá trị NOT NULL)
    IF NEW.ThoiGianKetThuc IS NOT NULL AND OLD.ThoiGianKetThuc IS NULL THEN
        -- 1. Cập nhật trạng thái máy tính thành 'Trống'
        UPDATE MayTinh
        SET TrangThai = 'Khả dụng'
        WHERE MaMay = NEW.MaMay;

        -- 2. Tính toán và trừ tiền tài khoản
        SELECT lm.GiaTheoGio INTO var_GiaTheoGio
        FROM MayTinh mt
        JOIN LoaiMay lm ON mt.MaLoaiMay = lm.MaLoaiMay
        WHERE mt.MaMay = NEW.MaMay;

        SET var_duration_seconds = TIMESTAMPDIFF(SECOND, NEW.ThoiGianBatDau, NEW.ThoiGianKetThuc);
        
        IF var_duration_seconds < 0 THEN -- Đảm bảo thời gian không âm
            SET var_duration_seconds = 0; 
        END IF;

        IF var_GiaTheoGio > 0 AND var_duration_seconds > 0 THEN
            SET var_cost = ROUND((var_duration_seconds / 3600.0) * var_GiaTheoGio, 2);
            
            IF var_cost > 0 THEN
                UPDATE TaiKhoan
                SET SoTienConLai = SoTienConLai - var_cost
                WHERE MaTK = NEW.MaTK;
            END IF;
        END IF;
    END IF;
END$$
DELIMITER ;


-- 4. Trigger tự động trừ tiền dịch vụ khi tạo hóa đơn dịch vụ mới
drop trigger if exists trg_After_CT_HoaDonDV_Insert_DeductItemCost;

DELIMITER $$

CREATE TRIGGER trg_After_CT_HoaDonDV_Insert_DeductItemCost
AFTER INSERT ON CT_HoaDonDV
FOR EACH ROW
BEGIN
    DECLARE var_DonGia_DV DECIMAL(10, 2);
    DECLARE var_ItemCost_TruocUuDai DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE var_MaTK_KH VARCHAR(10);
    DECLARE var_MaUuDai_HD VARCHAR(10);
    DECLARE var_MucUuDai_Percent DECIMAL(5, 2) DEFAULT 0.00;
    DECLARE var_SoTienGiam_Item DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE var_FinalItemCost_SauUuDai DECIMAL(12, 2) DEFAULT 0.00;
    DECLARE var_TenDV VARCHAR(100);
    -- 1. Get price and name of the service that was just added
    SELECT dv.DonGia, dv.TenDV
    INTO var_DonGia_DV, var_TenDV
    FROM DichVu dv
    WHERE dv.MaDV = NEW.MaDV;
    
    -- 2. Calculate the initial cost of this item
    SET var_ItemCost_TruocUuDai = NEW.SoLuong * var_DonGia_DV;

    -- 3. Get Account ID (MaTK) and Discount ID (MaUuDai) from the corresponding Invoice (HoaDonDV)
    SELECT hddv.MaTK, hddv.MaUuDai
    INTO var_MaTK_KH, var_MaUuDai_HD
    FROM HoaDonDV hddv
    WHERE hddv.MaHD = NEW.MaHD;

    -- 4. Apply discount (if any) for this item
    IF var_MaUuDai_HD IS NOT NULL THEN
        SELECT COALESCE(ud.MucUuDai, 0)
        INTO var_MucUuDai_Percent
        FROM UuDai ud
        WHERE ud.MaUuDai = var_MaUuDai_HD;

        -- Only apply if discount rate is valid (0 < MucUuDai <= 100)
        IF var_MucUuDai_Percent > 0 AND var_MucUuDai_Percent <= 100 THEN
            SET var_SoTienGiam_Item = ROUND(var_ItemCost_TruocUuDai * (var_MucUuDai_Percent / 100.0), 2);
        END IF;
    END IF;

    -- 5. Calculate the final payment amount for this item
    SET var_FinalItemCost_SauUuDai = var_ItemCost_TruocUuDai - var_SoTienGiam_Item;

    -- 6. Xử lý tiền tài khoản dựa trên mã dịch vụ
    IF NEW.MaDV IN ('DV001', 'DV002', 'DV003') THEN
        -- Nếu là dịch vụ đặc biệt, cộng tiền vào tài khoản
        UPDATE TaiKhoan
        SET SoTienConLai = SoTienConLai + var_FinalItemCost_SauUuDai
        WHERE MaTK = var_MaTK_KH;
    ELSE
        -- Nếu là dịch vụ thông thường, trừ tiền tài khoản
        IF var_FinalItemCost_SauUuDai > 0 THEN
            UPDATE TaiKhoan
            SET SoTienConLai = SoTienConLai - var_FinalItemCost_SauUuDai
            WHERE MaTK = var_MaTK_KH;
        END IF;
    END IF;

END$$

DELIMITER ;

show tables;


-- 5. Trigger cảnh báo hết hàng
DELIMITER $$
CREATE TRIGGER trg_Before_CT_HoaDonDV_Insert_CheckServiceAvailability
BEFORE INSERT ON CT_HoaDonDV
FOR EACH ROW
BEGIN
    DECLARE service_status VARCHAR(45);
    DECLARE service_name_val VARCHAR(100);

    -- Lấy thông tin Tên dịch vụ, Trạng thái từ bảng DichVu
    SELECT TenDV, TrangThaiDichVu
    INTO service_name_val, service_status
    FROM DichVu
    WHERE MaDV = NEW.MaDV;

    -- Kiểm tra xem dịch vụ có tồn tại không
    IF service_name_val IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Dịch vụ với không tồn tại trong danh mục.';
    END IF;

    -- 1. Kiểm tra theo yêu cầu chính: "nếu như trạng thái là hết hàng thì không được"
    IF service_status = 'Hết hàng' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Dịch vụ hiện đang ở trạng thái "Hết hàng" và không thể thêm vào hóa đơn.';
    END IF;
    
    -- 2. Kiểm tra các trạng thái không cho phép bán khác (ví dụ: 'Ngừng kinh doanh')
    IF service_status = 'Ngừng kinh doanh' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Dịch vụ đã "Ngừng kinh doanh" và không thể thêm vào hóa đơn.';
    END IF;
    
    -- 3. Kiểm tra các trạng thái không cho phép bán khác (ví dụ: 'Sắp có')
    IF service_status = 'Sắp có' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Dịch vụ  "Sắp có" và không thể thêm vào hóa đơn.';
    END IF;
END$$
DELIMITER ;

-- 6. Trigger ngăn chặn ThoiGianKetThuc của phiên sử dụng sớm hơn ThoiGianBatDau
DELIMITER $$
CREATE TRIGGER trg_Before_PhienSuDung_Update_ValidateThoiGianKetThuc
BEFORE UPDATE ON PhienSuDung
FOR EACH ROW
BEGIN
    -- Chỉ kiểm tra nếu ThoiGianKetThuc đang được cập nhật và không phải là NULL
    IF NEW.ThoiGianKetThuc IS NOT NULL THEN
        -- So sánh với ThoiGianBatDau của bản ghi hiện tại (OLD.ThoiGianBatDau)
        -- vì ThoiGianBatDau không nên thay đổi khi kết thúc phiên.
        IF NEW.ThoiGianKetThuc < OLD.ThoiGianBatDau THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Lỗi: Thời gian kết thúc phiên không thể sớm hơn thời gian bắt đầu phiên.';
        END IF;
    END IF;
END$$
DELIMITER ;

-- 7. Trigger ngăn chặn MucUuDai (Mức Ưu Đãi) nằm ngoài khoảng hợp lệ (0-100%)
-- a. Cho thao tác INSERT:
DELIMITER $$
CREATE TRIGGER trg_Before_UuDai_Insert_ValidateMucUuDai
BEFORE INSERT ON UuDai
FOR EACH ROW
BEGIN
    IF NEW.MucUuDai IS NOT NULL AND (NEW.MucUuDai < 0 OR NEW.MucUuDai > 100) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Mức ưu đãi phải nằm trong khoảng từ 0 đến 100 (%).';
    END IF;
END$$
DELIMITER ;

-- b. Cho thao tác UPDATE:
DELIMITER $$
CREATE TRIGGER trg_Before_UuDai_Update_ValidateMucUuDai
BEFORE UPDATE ON UuDai
FOR EACH ROW
BEGIN
    IF NEW.MucUuDai IS NOT NULL AND (NEW.MucUuDai < 0 OR NEW.MucUuDai > 100) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Mức ưu đãi phải nằm trong khoảng từ 0 đến 100 (%).';
    END IF;
END$$
DELIMITER ;

-- 8. Trigger ngăn chặn SoLuong (Số Lượng) trong chi tiết hóa đơn dịch vụ (CT_HoaDonDV) là số không dương
-- a. Cho thao tác INSERT:
DELIMITER $$
CREATE TRIGGER trg_Before_CT_HoaDonDV_Insert_ValidateSoLuong
BEFORE INSERT ON CT_HoaDonDV
FOR EACH ROW
BEGIN
    IF NEW.SoLuong <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Số lượng dịch vụ trong hóa đơn phải là một số nguyên dương.';
    END IF;
END$$
DELIMITER ;

-- b. Cho thao tác UPDATE:
DELIMITER $$
CREATE TRIGGER trg_Before_CT_HoaDonDV_Update_ValidateSoLuong
BEFORE UPDATE ON CT_HoaDonDV
FOR EACH ROW
BEGIN
    IF NEW.SoLuong <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Lỗi: Số lượng dịch vụ trong hóa đơn phải là một số nguyên dương.';
    END IF;
END$$
DELIMITER ;

-- -----------------------------------------------------
-- CHECK TRIGGERS
-- -----------------------------------------------------

SHOW TRIGGERS;
