package com.example.stores.service.impl; // Bỏ .computerstore

import com.example.stores.model.Manager;
import com.example.stores.repository.ManagerRepository;
// import com.example.stores.repository.impl.ManagerRepositoryImpl; // Không cần import trực tiếp impl ở đây nếu dùng DI
import com.example.stores.service.ManagerService;
import com.example.stores.util.PasswordUtil; // Giả sử PasswordUtil ở package util

import java.util.Optional;
import java.util.regex.Pattern;


public class ManagerServiceImpl implements ManagerService {

    private final ManagerRepository managerRepository;

    // Email regex pattern (có thể chuyển vào Util class nếu dùng nhiều nơi)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    // Phone regex pattern (Việt Nam)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(03|07|08|09)\\d{8}$");


    // Constructor Injection (Cách tốt để quản lý dependency)
    // Bạn sẽ cần khởi tạo ManagerRepositoryImpl ở đâu đó (ví dụ trong MainApp hoặc Controller)
    // và truyền vào đây.
    public ManagerServiceImpl(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @Override
    public Optional<Manager> getManagerProfile(int managerId) {
        return managerRepository.findById(managerId);
    }

    @Override
    public boolean updateManagerProfile(Manager manager) throws IllegalArgumentException {
        if (manager == null) {
            throw new IllegalArgumentException("Thông tin Manager không được null.");
        }
        if (manager.getManagerID() <= 0) { // Giả sử ID luôn > 0
            throw new IllegalArgumentException("Manager ID không hợp lệ.");
        }

        // Validate dữ liệu
        if (manager.getFullName() == null || manager.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        validateEmail(manager.getEmail());
        validatePhone(manager.getPhone());
        // Các validate khác cho gender, address nếu cần

        // Kiểm tra xem Manager có tồn tại không trước khi cập nhật
        Optional<Manager> existingManagerOpt = managerRepository.findById(manager.getManagerID());
        if (existingManagerOpt.isEmpty()) {
            // Hoặc throw new RuntimeException("Không tìm thấy Manager để cập nhật.");
            return false; // Không tìm thấy manager để cập nhật
        }

        // Nếu mật khẩu mới được cung cấp (khác rỗng), thì hash nó
        // Nếu người dùng không nhập gì vào ô mật khẩu trên UI, controller nên gửi password là null hoặc rỗng
        if (manager.getPassword() != null && !manager.getPassword().trim().isEmpty()) {
            // Kiểm tra độ dài mật khẩu hoặc các yêu cầu khác nếu cần
            if (manager.getPassword().trim().length() < 6) { // Ví dụ: mật khẩu ít nhất 6 ký tự
                throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
            manager.setPassword(PasswordUtil.hashPassword(manager.getPassword().trim()));
        } else {
            // Nếu không có mật khẩu mới, giữ lại mật khẩu cũ từ DB (không truyền vào câu lệnh update)
            // Lớp RepositoryImpl đã xử lý việc này: chỉ update password nếu nó được set.
            // Để đảm bảo, chúng ta có thể set lại password của đối tượng manager thành password cũ.
            manager.setPassword(null); // Hoặc existingManagerOpt.get().getPassword() nếu Repository cần nó
            // Nhưng cách tốt nhất là RepositoryImpl tự xử lý không update nếu field password trong model là null/empty
        }

        return managerRepository.update(manager);
    }

    @Override
    public Optional<Manager> login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return Optional.empty(); // Hoặc ném IllegalArgumentException
        }
        // Tìm manager theo username
        Optional<Manager> managerOpt = managerRepository.findByUsername(username.trim());
        if (managerOpt.isPresent()) {
            Manager manager = managerOpt.get();
            // Kiểm tra mật khẩu (đã hash trong DB) với mật khẩu người dùng nhập (chưa hash)
            if (PasswordUtil.checkPassword(password, manager.getPassword())) {
                return Optional.of(manager); // Đăng nhập thành công
            }
        }
        return Optional.empty(); // Username không tồn tại hoặc mật khẩu sai
    }

    private void validateEmail(String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        // Việc kiểm tra UNIQUE đã có ở DB constraint, không cần check ở đây trừ khi muốn thông báo sớm.
    }

    private void validatePhone(String phone) throws IllegalArgumentException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("Định dạng số điện thoại không hợp lệ. Phải có 10 số và bắt đầu bằng 03, 07, 08, 09.");
        }
    }
}