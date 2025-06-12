package com.example.stores.repository.impl;

import com.example.stores.config.DatabaseConnection;
import com.example.stores.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class để truy xuất và xử lý dữ liệu sản phẩm
 */
public class ProductRepository {
    
    /**
     * Lấy tất cả sản phẩm
     * @return Danh sách sản phẩm
     */
    public static List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT p.*, c.categoryName FROM Products p JOIN Categories c ON p.categoryID = c.categoryID";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = DatabaseConnection.getConnection();
        stmt = conn.prepareStatement(sql);
        rs = stmt.executeQuery();
        
        // Debug: Kiểm tra ResultSet
        System.out.println("Đang truy vấn dữ liệu sản phẩm...");
        
        while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
            
            // Debug: In ra thông tin sản phẩm đã nhận
            System.out.println("Đã tìm thấy sản phẩm: " + 
                rs.getString("productName") + " - " + rs.getDouble("price"));
        }
        
        // Debug: In tổng số sản phẩm
        System.out.println("Tổng số sản phẩm tìm thấy: " + products.size());
        
    } catch (SQLException e) {
        System.err.println("Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // Đóng tài nguyên kết nối
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    return products;
}
    
    /**
     * Lấy sản phẩm theo danh mục
     * @param categoryName Tên danh mục
     * @return Danh sách sản phẩm thuộc danh mục
     */
    public static List<Product> getProductsByCategory(String categoryName) {
        List<Product> products = new ArrayList<>();
        
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("Lỗi: Tên danh mục rỗng");
            return products;
        }
        
        System.out.println("Đang tìm sản phẩm với danh mục: " + categoryName);
        
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                     "JOIN Categories c ON p.categoryID = c.categoryID " +
                     "WHERE c.categoryName = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryName);
            System.out.println("SQL: " + sql + " [param: " + categoryName + "]");
            
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                    count++;
                }
                System.out.println("Tìm thấy " + count + " sản phẩm với danh mục: " + categoryName);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm theo danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Sắp xếp sản phẩm theo giá tăng dần
     * @return Danh sách sản phẩm đã sắp xếp
     */
    public static List<Product> getProductsSortedByPriceAsc() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT p.*, c.categoryName FROM Products p " + 
                 "JOIN Categories c ON p.categoryID = c.categoryID " + 
                 "ORDER BY p.price ASC";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = DatabaseConnection.getConnection();
        stmt = conn.prepareStatement(sql);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
        }
        
    } catch (SQLException e) {
        System.err.println("Lỗi khi tải danh sách sản phẩm sắp xếp theo giá tăng dần: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // Đóng tài nguyên kết nối
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    return products;
}
    
    /**
     * Sắp xếp sản phẩm theo giá giảm dần
     * @return Danh sách sản phẩm đã sắp xếp
     */
   // Tương tự cập nhật các phương thức khác trong repository
public static List<Product> getProductsSortedByPriceDesc() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT p.*, c.categoryName FROM Products p " +
                 "JOIN Categories c ON p.categoryID = c.categoryID " +
                 "ORDER BY p.price DESC";
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = DatabaseConnection.getConnection();
        stmt = conn.prepareStatement(sql);
        rs = stmt.executeQuery();
        
        while (rs.next()) {
            products.add(mapResultSetToProduct(rs));
        }
        
    } catch (SQLException e) {
        System.err.println("Lỗi khi tải danh sách sản phẩm sắp xếp theo giá giảm dần: " + e.getMessage());
        e.printStackTrace();
    } finally {
        // Đóng tài nguyên kết nối
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    return products;
}
    
    /**
     * Tìm kiếm sản phẩm theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách sản phẩm phù hợp
     */
    public static List<Product> searchProducts(String keyword) {
        try {
            // Log truy vấn để debug
            System.out.println("Đang tìm kiếm trong DB với từ khóa: " + keyword);

            // Làm sạch keyword để tìm kiếm an toàn
            String searchTerm = "%" + keyword.toLowerCase() + "%";

            // SỬA TÊN BẢNG THÀNH PRODUCTS (có s ở cuối) và cập nhật tên cột
            String sql = "SELECT p.*, c.categoryName FROM Products p " +
                    "JOIN Categories c ON p.categoryID = c.categoryID " +
                    "WHERE LOWER(p.productName) LIKE ? OR LOWER(p.description) LIKE ?";

            List<Product> results = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Product product = mapResultSetToProduct(rs);
                        results.add(product);
                    }
                }
            }

            System.out.println("Kết quả tìm kiếm: " + results.size() + " sản phẩm");
            return results;

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Tìm kiếm sản phẩm trực tiếp khi người dùng gõ
     */
    public static List<Product> searchProductsLive(String keyword) {
        try {
            // Làm sạch keyword để tìm kiếm an toàn
            String searchTerm = "%" + keyword.toLowerCase() + "%";

            // Tạo truy vấn SQL
            String sql = "SELECT p.*, c.categoryName FROM Products p " +
                    "JOIN Categories c ON p.categoryID = c.categoryID " +
                    "WHERE LOWER(p.productName) LIKE ? " +
                    "ORDER BY " +
                    "CASE WHEN LOWER(p.productName) LIKE ? THEN 0 ELSE 1 END, " +
                    "p.productName ASC";

            List<Product> results = new ArrayList<>();
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Đặt tham số cho câu truy vấn
                stmt.setString(1, searchTerm);
                stmt.setString(2, searchTerm.startsWith("%") ?
                        searchTerm.substring(1) : searchTerm); // Để ưu tiên kết quả bắt đầu bằng từ khóa

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Product product = mapResultSetToProduct(rs);
                        results.add(product);
                    }
                }
            }

            return results;

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm sản phẩm trực tiếp: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Lọc sản phẩm theo khoảng giá
     * @param min Giá tối thiểu
     * @param max Giá tối đa
     * @return Danh sách sản phẩm phù hợp
     */
    public static List<Product> getProductsByPriceRange(Double min, Double max) {
        List<Product> products = new ArrayList<>();
        
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT p.*, c.categoryName FROM Products p " +
            "JOIN Categories c ON p.categoryID = c.categoryID WHERE 1=1"
        );
        
        if (min != null) {
            sqlBuilder.append(" AND p.price >= ?");
        }
        
        if (max != null) {
            sqlBuilder.append(" AND p.price <= ?");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            int paramIndex = 1;
            
            if (min != null) {
                stmt.setDouble(paramIndex++, min);
            }
            
            if (max != null) {
                stmt.setDouble(paramIndex, max);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lọc sản phẩm theo khoảng giá: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Chuyển đổi kết quả truy vấn thành đối tượng Product
     * @param rs ResultSet chứa dữ liệu sản phẩm
     * @return Đối tượng Product
     * @throws SQLException Nếu có lỗi khi đọc dữ liệu
     */
    private static Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductID(rs.getString("productID"));
        product.setProductName(rs.getString("productName"));
        product.setCategoryID(rs.getString("categoryID"));
        product.setCategoryName(rs.getString("categoryName"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setImagePath(rs.getString("imagePath"));

        // Các trường bổ sung (handle null)
        try {
            product.setPriceCost(rs.getDouble("priceCost"));
            product.setQuantity(rs.getInt("quantity"));
            product.setStatus(rs.getString("status"));
            product.setPurchaseCount(rs.getInt("purchaseCount")); // Thêm đọc purchaseCount

            if (rs.getTimestamp("createdAt") != null) {
                product.setCreatedAt(rs.getTimestamp("createdAt"));
            }
        } catch (SQLException e) {
            // Không xử lý nếu các trường này không có trong kết quả truy vấn
        }

        return product;
    }
    
    /**
     * Lấy sản phẩm theo ID
     * @param productId ID sản phẩm
     * @return Sản phẩm tìm thấy hoặc null nếu không tìm thấy
     */    
    public static Product getProductById(String productId) {
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                     "JOIN Categories c ON p.categoryID = c.categoryID " +
                     "WHERE p.productID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding product by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Lấy sản phẩm theo ID danh mục
     * @param categoryId ID danh mục
     * @return Danh sách sản phẩm thuộc danh mục
     */
    public static List<Product> getProductsByCategoryId(String categoryId) {
        List<Product> products = new ArrayList<>();
        
        if (categoryId == null || categoryId.trim().isEmpty()) {
            System.err.println("Lỗi: ID danh mục rỗng");
            return products;
        }
        
        System.out.println("Đang tìm sản phẩm với ID danh mục: " + categoryId);
        
        String sql = "SELECT p.*, c.categoryName FROM Products p " +
                     "JOIN Categories c ON p.categoryID = c.categoryID " +
                     "WHERE p.categoryID = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                    count++;
                }
                System.out.println("Tìm thấy " + count + " sản phẩm với ID danh mục: " + categoryId);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm theo ID danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    /**
     * Lấy danh sách sản phẩm nổi bật (theo lượt mua nhiều nhất và đánh giá cao nhất)
     * @param limit Số lượng sản phẩm cần lấy
     * @return Danh sách sản phẩm nổi bật
     */
    public static List<Product> getFeaturedProducts(int limit) {
        List<Product> products = new ArrayList<>();

        // SQL sửa đổi để ưu tiên purchaseCount và rating từ ProductReview
        String sql =
                "SELECT TOP " + limit + " p.*, c.categoryName, " +
                        "ISNULL(AVG(pr.rating), 0) as avgRating " +
                        "FROM Products p " +
                        "JOIN Categories c ON p.categoryID = c.categoryID " +
                        "LEFT JOIN ProductReview pr ON p.productID = pr.productID " +
                        "WHERE p.quantity > 0 " +  // Chỉ lấy sản phẩm còn hàng
                        "GROUP BY p.productID, p.productName, p.categoryID, p.description, " +
                        "p.price, p.priceCost, p.imagePath, p.quantity, p.status, p.createdAt, " +
                        "p.purchaseCount, c.categoryName " +
                        "ORDER BY p.purchaseCount DESC, avgRating DESC, p.createdAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                // Nếu có thêm thông tin đánh giá, có thể lưu vào product
                try {
                    double avgRating = rs.getDouble("avgRating");
                    // Có thể lưu avgRating vào product nếu cần
                } catch (SQLException e) {
                    // Bỏ qua nếu không có cột avgRating
                }
                products.add(product);
            }

            // Nếu không đủ sản phẩm có lượt mua hoặc đánh giá, lấy thêm sản phẩm mới nhất
            if (products.size() < limit) {
                String fallbackSql =
                        "SELECT TOP " + (limit - products.size()) + " p.*, c.categoryName " +
                                "FROM Products p " +
                                "JOIN Categories c ON p.categoryID = c.categoryID " +
                                "WHERE p.productID NOT IN (SELECT productID FROM " +
                                "   (SELECT TOP " + products.size() + " productID FROM Products ORDER BY purchaseCount DESC) as t) " +
                                "AND p.quantity > 0 " +
                                "ORDER BY p.createdAt DESC";

                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackSql);
                     ResultSet fallbackRs = fallbackStmt.executeQuery()) {

                    while (fallbackRs.next() && products.size() < limit) {
                        products.add(mapResultSetToProduct(fallbackRs));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm nổi bật: " + e.getMessage());
            e.printStackTrace();

            // Nếu lỗi thì lấy sản phẩm mới nhất
            products = getRecentProducts(limit);
        }

        return products;
    }
    
    /**
     * Lấy danh sách sản phẩm mới nhất
     * @param limit Số lượng sản phẩm cần lấy
     * @return Danh sách sản phẩm mới nhất
     */
    public static List<Product> getRecentProducts(int limit) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT TOP " + limit + " p.*, c.categoryName " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.categoryID = c.categoryID " +
                     "ORDER BY p.createdAt DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm mới nhất: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
    
    /**
     * Lấy tất cả loại sản phẩm và một số sản phẩm từ mỗi loại
     * @param productsPerCategory Số sản phẩm cần lấy cho mỗi loại
     * @return Map với key là Category và value là List<Product>
     */
    public static Map<String, List<Product>> getProductsByCategories(int productsPerCategory) {
        Map<String, List<Product>> result = new LinkedHashMap<>();
        
        // Lấy danh sách tất cả các danh mục
        String categorySql = "SELECT categoryID, categoryName FROM Categories";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement categoryStmt = conn.prepareStatement(categorySql);
             ResultSet categoryRs = categoryStmt.executeQuery()) {
            
            while (categoryRs.next()) {
                String categoryId = categoryRs.getString("categoryID");
                String categoryName = categoryRs.getString("categoryName");
                
                // Lấy sản phẩm cho mỗi danh mục
                String productSql = "SELECT TOP " + productsPerCategory + " p.*, c.categoryName " +
                                    "FROM Products p " +
                                    "JOIN Categories c ON p.categoryID = c.categoryID " +
                                    "WHERE p.categoryID = ? " +
                                    "ORDER BY p.createdAt DESC";
                
                try (PreparedStatement productStmt = conn.prepareStatement(productSql)) {
                    productStmt.setString(1, categoryId);
                    
                    try (ResultSet productRs = productStmt.executeQuery()) {
                        List<Product> categoryProducts = new ArrayList<>();
                        while (productRs.next()) {
                            categoryProducts.add(mapResultSetToProduct(productRs));
                        }
                        
                        // Chỉ thêm vào kết quả nếu có sản phẩm trong danh mục
                        if (!categoryProducts.isEmpty()) {
                            result.put(categoryName, categoryProducts);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm theo danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
        /**
     * Lấy danh sách sản phẩm theo tên danh mục với số lượng giới hạn
     * @param categoryName Tên danh mục
     * @param limit Số lượng tối đa sản phẩm cần lấy
     * @return Danh sách sản phẩm
     */
    public static List<Product> getProductsLimitByCategory(String categoryName, int limit) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT TOP " + limit + " p.*, c.categoryName " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.categoryID = c.categoryID " +
                     "WHERE c.categoryName = ? " +
                     "ORDER BY p.createdAt DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải sản phẩm theo danh mục " + categoryName + ": " + e.getMessage());
        }
        
        return products;
    }
    // Thêm phương thức mới để cập nhật đường dẫn hình ảnh trong database
    public static boolean updateImagePaths() {
        String sql = "UPDATE Products SET imagePath = REPLACE(imagePath, '..\\images\\', '/com/example/stores/images/products/') WHERE imagePath LIKE '..\\\\images\\\\%'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Đã cập nhật " + rowsUpdated + " đường dẫn hình ảnh trong database");
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đường dẫn hình ảnh: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // Thêm phương thức này vào lớp ProductRepository hiện tại
    /**
     * Cập nhật số lượng sản phẩm sau khi bán hàng
     * @param productId ID của sản phẩm
     * @param newQuantity Số lượng mới
     * @param purchaseQuantity Số lượng mua (dùng để tăng purchaseCount)
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public static boolean updateProductQuantity(String productId, int newQuantity, int purchaseQuantity) {
        String sql = "UPDATE Products SET quantity = ?, purchaseCount = purchaseCount + ? WHERE productID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, purchaseQuantity);  // Tăng purchaseCount lên theo số lượng đã mua
            stmt.setString(3, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật số lượng sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Phương thức gốc (giữ lại để tương thích với code hiện tại)
     */
    public static boolean updateProductQuantity(String productId, int newQuantity) {
        // Để đảm bảo tương thích với code cũ, gọi phương thức mới với purchaseQuantity = 0
        // (không tăng purchaseCount)
        return updateProductQuantity(productId, newQuantity, 0);
    }

    /**
     * Khôi phục số lượng sản phẩm sau khi hủy đơn
     * @param productId ID của sản phẩm
     * @param restoreQuantity Số lượng cần khôi phục
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public static boolean restoreProductQuantity(String productId, int restoreQuantity) {
        String sql = "UPDATE Products SET quantity = quantity + ?, purchaseCount = purchaseCount - ? WHERE productID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restoreQuantity);
            stmt.setInt(2, restoreQuantity);  // Giảm purchaseCount
            stmt.setString(3, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khôi phục số lượng sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Cập nhật đường dẫn hình ảnh cho sản phẩm
     * @param productId ID sản phẩm
     * @param imagePath Đường dẫn hình ảnh mới
     * @return true nếu cập nhật thành công, false nếu có lỗi
     */
    public static boolean updateProductImagePath(String productId, String imagePath) {
        String sql = "UPDATE Products SET imagePath = ? WHERE productID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, imagePath);
            stmt.setString(2, productId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đường dẫn ảnh sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}