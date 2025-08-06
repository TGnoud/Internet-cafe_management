
-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS quan_ly_quan_net;

-- Sử dụng cơ sở dữ liệu vừa tạo
USE quan_ly_quan_net;

-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`LoaiMay`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`LoaiMay` (
  `MaLoaiMay` VARCHAR(10) NOT NULL,
  `GiaTheoGio` DECIMAL(10, 2) NOT NULL,
  `MoTa` VARCHAR(255) NULL,
  PRIMARY KEY (`MaLoaiMay`)
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`MayTinh`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`MayTinh` (
  `MaMay` VARCHAR(10) NOT NULL,
  `TenMay` VARCHAR(45) NOT NULL,
  `TrangThai` VARCHAR(45) NULL COMMENT 'Ví dụ: Khả dụng, Bảo trì ', 
  `MaLoaiMay` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`MaMay`),
  INDEX `fk_MayTinh_LoaiMay1_idx` (`MaLoaiMay` ASC) VISIBLE,
  CONSTRAINT `fk_MayTinh_LoaiMay1`
    FOREIGN KEY (`MaLoaiMay`)
    REFERENCES `quan_ly_quan_net`.`LoaiMay` (`MaLoaiMay`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`UuDai`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`UuDai` (
  `MaUuDai` VARCHAR(10) NOT NULL,
  `MucUuDai` DECIMAL(5, 2) NULL COMMENT 'Phần trăm ưu đãi',
  `NoiDung` VARCHAR(255) NULL,
  PRIMARY KEY (`MaUuDai`)
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`LoaiKH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`LoaiKH` (
  `MaLoaiKH` VARCHAR(10) NOT NULL,
  `TenLoai` VARCHAR(45) NOT NULL,
  `MaUuDai` VARCHAR(10) NULL,
  PRIMARY KEY (`MaLoaiKH`),
  INDEX `fk_LoaiKH_UuDai1_idx` (`MaUuDai` ASC) VISIBLE,
  CONSTRAINT `fk_LoaiKH_UuDai1`
    FOREIGN KEY (`MaUuDai`)
    REFERENCES `quan_ly_quan_net`.`UuDai` (`MaUuDai`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`KhachHang`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`KhachHang` (
  `MaKH` VARCHAR(10) NOT NULL,
  `HoTen` VARCHAR(100) NOT NULL,
  `SoDienThoai` VARCHAR(15) NOT NULL,
  `GioiTinh` VARCHAR(10) NULL,
  `MaLoaiKH` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`MaKH`),
  UNIQUE INDEX `SoDienThoai_UNIQUE` (`SoDienThoai` ASC) VISIBLE,
  INDEX `fk_KhachHang_LoaiKH1_idx` (`MaLoaiKH` ASC) VISIBLE,
  CONSTRAINT `fk_KhachHang_LoaiKH1`
    FOREIGN KEY (`MaLoaiKH`)
    REFERENCES `quan_ly_quan_net`.`LoaiKH` (`MaLoaiKH`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`TaiKhoan`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`TaiKhoan` (
  `MaTK` VARCHAR(10) NOT NULL,
  `TenTK` VARCHAR(45) NOT NULL,
  `MatKhau` VARCHAR(255) NOT NULL COMMENT 'Nên được lưu dưới dạng mã hóa (hash)',
  `SoTienConLai` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `MaKH` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`MaTK`),
  UNIQUE INDEX `TenTK_UNIQUE` (`TenTK` ASC) VISIBLE,
  INDEX `fk_TaiKhoan_KhachHang1_idx` (`MaKH` ASC) VISIBLE,
  UNIQUE INDEX `MaKH_UNIQUE` (`MaKH` ASC) VISIBLE, -- Quan hệ 1-1 giữa KhachHang và TaiKhoan
  CONSTRAINT `fk_TaiKhoan_KhachHang1`
    FOREIGN KEY (`MaKH`)
    REFERENCES `quan_ly_quan_net`.`KhachHang` (`MaKH`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`PhienSuDung`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`PhienSuDung` (
  `MaPhien` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `MaMay` VARCHAR(10) NOT NULL,
  `MaTK` VARCHAR(10) NOT NULL,
  `ThoiGianBatDau` DATETIME NOT NULL,
  `ThoiGianKetThuc` DATETIME NULL,
  PRIMARY KEY (`MaPhien`),
  INDEX `fk_PhienSuDung_MayTinh1_idx` (`MaMay` ASC) VISIBLE,
  INDEX `fk_PhienSuDung_TaiKhoan1_idx` (`MaTK` ASC) VISIBLE,
  CONSTRAINT `fk_PhienSuDung_MayTinh1`
    FOREIGN KEY (`MaMay`)
    REFERENCES `quan_ly_quan_net`.`MayTinh` (`MaMay`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PhienSuDung_TaiKhoan1`
    FOREIGN KEY (`MaTK`)
    REFERENCES `quan_ly_quan_net`.`TaiKhoan` (`MaTK`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`DichVu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`DichVu` (
  `MaDV` VARCHAR(10) NOT NULL,
  `TenDV` VARCHAR(100) NOT NULL,
  `DonGia` DECIMAL(10, 2) NOT NULL,
  `TrangThaiDichVu` VARCHAR(45) NULL COMMENT 'Ví dụ: Còn hàng, Hết hàng',
  PRIMARY KEY (`MaDV`)
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`ChucVu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`ChucVu` (
  `MaChucVu` VARCHAR(10) NOT NULL,
  `TenChucVu` VARCHAR(45) NOT NULL,
  `LuongTheoGio` DECIMAL(10, 2) NULL,
  PRIMARY KEY (`MaChucVu`)
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`NhanVien`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`NhanVien` (
  `MaNV` VARCHAR(10) NOT NULL,
  `HoTen` VARCHAR(100) NOT NULL,
  `SoDienThoai` VARCHAR(15) NULL,
  `GioiTinh` VARCHAR(10) NULL,
  `NgaySinh` DATE NULL,
  `MaChucVu` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`MaNV`),
  INDEX `fk_NhanVien_ChucVu1_idx` (`MaChucVu` ASC) VISIBLE,
  CONSTRAINT `fk_NhanVien_ChucVu1`
    FOREIGN KEY (`MaChucVu`)
    REFERENCES `quan_ly_quan_net`.`ChucVu` (`MaChucVu`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`HoaDonDV`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`HoaDonDV` (
  `MaHD` varchar(15) NOT NULL,
  `ThoiDiemThanhToan` DATETIME NOT NULL,
  `MaTK` VARCHAR(10) NOT NULL,
  `MaNV` VARCHAR(10) NULL,
  `MaUuDai` VARCHAR(10) NULL,
  PRIMARY KEY (`MaHD`),
  INDEX `fk_HoaDonDV_TaiKhoan1_idx` (`MaTK` ASC) VISIBLE,
  INDEX `fk_HoaDonDV_NhanVien1_idx` (`MaNV` ASC) VISIBLE,
  INDEX `fk_HoaDonDV_UuDai1_idx` (`MaUuDai` ASC) VISIBLE,
  CONSTRAINT `fk_HoaDonDV_TaiKhoan1`
    FOREIGN KEY (`MaTK`)
    REFERENCES `quan_ly_quan_net`.`TaiKhoan` (`MaTK`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_HoaDonDV_NhanVien1`
    FOREIGN KEY (`MaNV`)
    REFERENCES `quan_ly_quan_net`.`NhanVien` (`MaNV`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_HoaDonDV_UuDai1`
    FOREIGN KEY (`MaUuDai`)
    REFERENCES `quan_ly_quan_net`.`UuDai` (`MaUuDai`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`CT_HoaDonDV`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`CT_HoaDonDV` (
  `MaHD` VARCHAR(15) NOT NULL,
  `MaDV` VARCHAR(10) NOT NULL,
  `SoLuong` INT NOT NULL,
  PRIMARY KEY (`MaHD`, `MaDV`),
  INDEX `fk_CT_HoaDonDV_DichVu1_idx` (`MaDV` ASC) VISIBLE,
  CONSTRAINT `fk_CT_HoaDonDV_HoaDonDV1`
    FOREIGN KEY (`MaHD`)
    REFERENCES `quan_ly_quan_net`.`HoaDonDV` (`MaHD`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CT_HoaDonDV_DichVu1`
    FOREIGN KEY (`MaDV`)
    REFERENCES `quan_ly_quan_net`.`DichVu` (`MaDV`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`CaLamViec`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`CaLamViec` (
  `MaCaLamViec` VARCHAR(10) NOT NULL,
  `HeSoLuong` DECIMAL(5, 2) NULL,
  `ThoiGianVaoLam` TIME NULL,
  `ThoiGianKetThuc` TIME NULL,
  PRIMARY KEY (`MaCaLamViec`)
) ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `quan_ly_quan_net`.`NhanVien_CaLamViec`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `quan_ly_quan_net`.`NhanVien_CaLamViec` (
  `MaNV` VARCHAR(10) NOT NULL,
  `MaCaLamViec` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`MaNV`, `MaCaLamViec`),
  INDEX `fk_NhanVien_CaLamViec_CaLamViec1_idx` (`MaCaLamViec` ASC) VISIBLE,
  CONSTRAINT `fk_NhanVien_CaLamViec_NhanVien1`
    FOREIGN KEY (`MaNV`)
    REFERENCES `quan_ly_quan_net`.`NhanVien` (`MaNV`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_NhanVien_CaLamViec_CaLamViec1`
    FOREIGN KEY (`MaCaLamViec`)
    REFERENCES `quan_ly_quan_net`.`CaLamViec` (`MaCaLamViec`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;


--
-- drop schema quan_ly_quan_net;
-- Dữ liệu cho 100 KhachHang



-- Dữ liệu mẫu cho bảng PhienSuDung (ví dụ 5 phiên)


