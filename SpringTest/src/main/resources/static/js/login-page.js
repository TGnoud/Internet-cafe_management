document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const apiErrorMessageDiv = document.getElementById('apiErrorMessage');

    if (loginForm) {
        loginForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            apiErrorMessageDiv.style.display = 'none'; // Ẩn thông báo lỗi cũ

            const tenTK = document.getElementById('tenTK').value;
            const matKhau = document.getElementById('matKhau').value;
            const maMayInput = document.getElementById('maMay');
            const maMay = maMayInput ? maMayInput.value : null; // Lấy mã máy nếu có

            const loginData = {
                tenTK: tenTK,
                matKhau: matKhau,
                maMay: maMay // API login của bạn nhận cả maMay
            };

            try {
                const response = await fetch('/api/auth/login', { // Gọi API login
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(loginData)
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || `Lỗi ${response.status}`);
                }

                const data = await response.json();
                console.log('Login successful:', data);

                // Lưu thông tin cần thiết vào localStorage/sessionStorage
                localStorage.setItem('maTK', data.maTK);
                localStorage.setItem('tenTK', data.tenTK);
                localStorage.setItem('maPhien', data.maPhien);
                // localStorage.setItem('userRole', data.vaiTro); // Cần API trả về vai trò

                // Chuyển hướng dựa trên vai trò (cần API trả về vai trò)
                // Ví dụ: Nếu API login trả về vai trò trong `data.vaiTro`
                // if (data.vaiTro === 'MANAGER' || data.vaiTro === 'ADMIN') { // Giả sử 'ADMIN' là vai trò quản lý
                //     window.location.href = '/admin/dashboard';
                // } else if (data.vaiTro === 'EMPLOYEE') {
                //     window.location.href = '/employee/dashboard';
                // } else if (data.vaiTro === 'CUSTOMER') {
                //     window.location.href = '/customer/dashboard';
                // } else {
                //     window.location.href = '/'; // Trang chủ mặc định
                // }
                // Tạm thời chuyển đến trang admin dashboard nếu đăng nhập thành công
                 alert('Đăng nhập thành công! Chuyển đến trang quản trị.');
                 window.location.href = '/admin/dashboard';


            } catch (error) {
                console.error('Login failed:', error);
                apiErrorMessageDiv.textContent = error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại.';
                apiErrorMessageDiv.style.display = 'block';
            }
        });
    }
});