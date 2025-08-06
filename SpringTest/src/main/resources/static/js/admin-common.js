function confirmDelete(apiUrl, itemName) {
    if (confirm(`Bạn có chắc chắn muốn xóa ${itemName} này không?`)) {
        fetch(apiUrl, {
            method: 'DELETE',
            headers: {
                // 'Authorization': 'Bearer ' + yourAuthToken, // Nếu API yêu cầu token
                // Thêm CSRF token nếu Spring Security của bạn yêu cầu
                // 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content') // Ví dụ
            }
        })
        .then(response => {
            if (response.ok) {
                // Có thể response.text() nếu API trả về text, hoặc response.json() nếu trả JSON
                return response.text().then(text => {
                    // Hiển thị thông báo thành công (có thể dùng alert hoặc một div trên trang)
                    alert(text || `${itemName.charAt(0).toUpperCase() + itemName.slice(1)} đã được xóa thành công.`);
                    window.location.reload(); // Tải lại trang để cập nhật danh sách
                });
            } else {
                return response.text().then(text => {
                    throw new Error(text || `Lỗi khi xóa ${itemName}.`);
                });
            }
        })
        .catch(error => {
            console.error(`Lỗi khi xóa ${itemName}:`, error);
            alert(`Lỗi khi xóa ${itemName}: ${error.message}`);
        });
    }
}