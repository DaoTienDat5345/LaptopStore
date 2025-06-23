-- Tạo DATABASE
CREATE DATABASE Computer_Storest COLLATE Vietnamese_CI_AS;
GO
USE Computer_Storest;
GO

-- BẢNG QUẢN LÝ
CREATE TABLE Manager (
    managerID INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE CHECK (email LIKE '%@%.%'),
    phone VARCHAR(10) NOT NULL UNIQUE CHECK (
        LEN(phone) = 10 AND (
            phone LIKE '03%' OR phone LIKE '07%' OR phone LIKE '09%'
        )
    ),
    gender NVARCHAR(10) CHECK (gender IN (N'Nam', N'Nữ', N'Khác')),
    birthDate DATE,
    address NVARCHAR(255),
    imageUrl NVARCHAR(255),
    createdAt DATETIME DEFAULT GETDATE()
);

-- BẢNG NHÂN VIÊN
CREATE TABLE Employee (
    employeeID INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE CHECK (email LIKE '%@%.%'),
    phone VARCHAR(10) NOT NULL UNIQUE CHECK (
        LEN(phone) = 10 AND (
            phone LIKE '03%' OR phone LIKE '07%' OR phone LIKE '09%'
        )
    ),
    gender NVARCHAR(10) CHECK (gender IN (N'Nam', N'Nữ', N'Khác')),
    birthDate DATE,
    address NVARCHAR(255),
    imageUrl NVARCHAR(255),
    position NVARCHAR(50) NOT NULL,
    salary DECIMAL(15,2) CHECK (salary >= 0),
    status NVARCHAR(20) DEFAULT N'Đang làm' CHECK (status IN (N'Đang làm', N'Nghỉ việc')),
    createdAt DATETIME DEFAULT GETDATE(),
    managerID INT,
    FOREIGN KEY (managerID) REFERENCES Manager(managerID)
);

-- BẢNG KHÁCH HÀNG
CREATE TABLE Customer (
    customerID INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE CHECK (email LIKE '%@%.%'),
    phone VARCHAR(10) NOT NULL UNIQUE CHECK (
        LEN(phone) = 10 AND (
            phone LIKE '03%' OR phone LIKE '07%' OR phone LIKE '09%'
        )
    ),
    gender NVARCHAR(10) CHECK (gender IN (N'Nam', N'Nữ', N'Khác')),
    birthDate DATE,
    address NVARCHAR(255),
    registeredAt DATETIME DEFAULT GETDATE(),
    isActive BIT DEFAULT 1
);

-- BẢNG KHO HÀNG
CREATE TABLE Warehouse (
    warehouseID INT IDENTITY(1,1) PRIMARY KEY,
    warehouseName NVARCHAR(100) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    phone VARCHAR(10) CHECK (
        LEN(phone) = 10 AND (
            phone LIKE '03%' OR phone LIKE '07%' OR phone LIKE '09%'
        )
    ),
    managerID INT,
    FOREIGN KEY (managerID) REFERENCES Manager(managerID)
);

-- BẢNG DANH MỤC SẢN PHẨM
CREATE TABLE Categories (
    categoryID VARCHAR(20) PRIMARY KEY,
    categoryCode VARCHAR(2) NOT NULL UNIQUE,
    categoryName NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    defaultWarrantyGroup INT DEFAULT 2
);

-- BẢNG SẢN PHẨM (productID tự sinh bằng trigger)
CREATE TABLE Products (
    productID VARCHAR(20) PRIMARY KEY,
    productName NVARCHAR(100) NOT NULL,
    categoryID VARCHAR(20) NOT NULL,
    description NVARCHAR(255),
    price DECIMAL(15,2) NOT NULL CHECK (price >= 0),
    priceCost DECIMAL(15,2) NOT NULL CHECK (priceCost >= 0),
    imagePath NVARCHAR(255),
    quantity INT NOT NULL CHECK (quantity >= 0),
    status AS (CASE WHEN quantity > 0 THEN N'Còn hàng' ELSE N'Hết hàng' END),
    createdAt DATETIME DEFAULT GETDATE(),
	purchaseCount INT DEFAULT 0 NOT NULL CHECK (purchaseCount >= 0),
    FOREIGN KEY (categoryID) REFERENCES Categories(categoryID)
);

-- BẢNG NHÀ CUNG CẤP
CREATE TABLE Supplier (
    supplierID INT IDENTITY(1,1) PRIMARY KEY,
    supplierName NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL CHECK (email LIKE '%@%.%'),
    phone VARCHAR(10) NOT NULL CHECK (
        LEN(phone) = 10 AND (
            phone LIKE '03%' OR phone LIKE '07%' OR phone LIKE '09%'
        )
    ),
    address NVARCHAR(200),
    taxCode VARCHAR(20)
);

-- BẢNG SẢN PHẨM – NHÀ CUNG CẤP (nhiều-nhiều)
CREATE TABLE ProductSupplier (
    productID VARCHAR(20) NOT NULL,
    supplierID INT NOT NULL,
    PRIMARY KEY (productID, supplierID),
    FOREIGN KEY (productID) REFERENCES Products(productID),
    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
);

-- BẢNG TỒN KHO
CREATE TABLE Inventory (
    inventoryID INT IDENTITY(1,1) PRIMARY KEY,
    warehouseID INT NOT NULL,
    productID VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    lastUpdate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (warehouseID) REFERENCES Warehouse(warehouseID),
    FOREIGN KEY (productID) REFERENCES Products(productID),
    CONSTRAINT UQ_Inv_Unique UNIQUE (warehouseID, productID)
);

-- PHIẾU NHẬP HÀNG
CREATE TABLE ImportReceipt (
    receiptID INT IDENTITY(1,1) PRIMARY KEY,
    supplierID INT NOT NULL,
    employeeID INT NOT NULL,
    warehouseID INT NOT NULL,
    importDate DATETIME DEFAULT GETDATE(),
    totalAmount DECIMAL(15,2) NOT NULL CHECK (totalAmount >= 0),
    note NVARCHAR(255),
    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID),
    FOREIGN KEY (employeeID) REFERENCES Employee(employeeID),
    FOREIGN KEY (warehouseID) REFERENCES Warehouse(warehouseID)
);

CREATE TABLE ImportReceiptDetail (
    receiptDetailID INT IDENTITY(1,1) PRIMARY KEY,
    receiptID INT NOT NULL,
    productID VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unitCost DECIMAL(15,2) NOT NULL CHECK (unitCost >= 0),
    FOREIGN KEY (receiptID) REFERENCES ImportReceipt(receiptID),
    FOREIGN KEY (productID) REFERENCES Products(productID)
);

CREATE TABLE Cart (
    cartID INT IDENTITY(1,1) PRIMARY KEY,
    customerID INT NOT NULL UNIQUE,
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE CartItem (
    cartItemID INT IDENTITY(1,1) PRIMARY KEY,
    cartID INT NOT NULL,
    productID VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    addedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (cartID) REFERENCES Cart(cartID),
    FOREIGN KEY (productID) REFERENCES Products(productID)
);

-- HÓA ĐƠN (Orders dùng orderID VARCHAR cho trigger)
CREATE TABLE Orders (
    orderID VARCHAR(20) PRIMARY KEY,
    orderDate DATETIME DEFAULT GETDATE(),
    totalAmount DECIMAL(15,2) NOT NULL CHECK (totalAmount >= 0),
    customerID INT NOT NULL,
    employeeID INT,
    orderStatus NVARCHAR(50) DEFAULT N'Đã xác nhận' CHECK (orderStatus IN (
        N'Đã xác nhận', N'Đã hủy'
    )),
    paymentMethod NVARCHAR(50), -- Phương thức thanh toán (COD, Chuyển khoản, Ví điện tử,...)
    recipientName NVARCHAR(50),
    recipientPhone VARCHAR(10) NOT NULL CHECK (
        LEN(recipientPhone) = 10 AND (
            recipientPhone LIKE '03%' OR recipientPhone LIKE '07%' OR recipientPhone LIKE '09%'
        )
    ),
    shippingAddress NVARCHAR(255),
    shippingFee DECIMAL(10,2) DEFAULT 0,
    notes NVARCHAR(MAX),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID),
    FOREIGN KEY (employeeID) REFERENCES Employee(employeeID)
);

CREATE TABLE OrderDetails (
    orderDetailsID VARCHAR(20) PRIMARY KEY,
    orderID VARCHAR(20) NOT NULL,
    productID VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unitPrice DECIMAL(15,2) NOT NULL, -- Giá gốc của sản phẩm
    warrantyType NVARCHAR(20) NOT NULL DEFAULT N'Thường' CHECK (warrantyType IN (N'Thường', N'Vàng')),
    warrantyPrice DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (warrantyPrice >= 0),
    subtotal AS (quantity * unitPrice + warrantyPrice) PERSISTED,
    note NVARCHAR(255),
    FOREIGN KEY (orderID) REFERENCES Orders(orderID),
    FOREIGN KEY (productID) REFERENCES Products(productID)
);

-- BẢNG BẢO HÀNH SẢN PHẨM
CREATE TABLE Warranty (
    warrantyID INT IDENTITY(1,1) PRIMARY KEY,
    orderDetailsID VARCHAR(20) NOT NULL UNIQUE,
    warrantyType NVARCHAR(20) NOT NULL CHECK (warrantyType IN (N'Thường', N'Vàng')),
    startDate DATE NOT NULL, 
    endDate DATE NOT NULL,
    status AS (CASE WHEN GETDATE() > endDate THEN N'Hết hạn' ELSE N'Còn hạn' END),
    notes NVARCHAR(MAX),
    FOREIGN KEY (orderDetailsID) REFERENCES OrderDetails(orderDetailsID)
);

CREATE TABLE ProductReview (
    reviewID INT IDENTITY(1,1) PRIMARY KEY,
    productID VARCHAR(20) NOT NULL,
    customerID INT NOT NULL,
    orderDetailsID VARCHAR(20) NOT NULL,
    rating TINYINT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX) NULL,
    reviewDate DATETIME DEFAULT GETDATE(),
    isApproved BIT DEFAULT 0,
    FOREIGN KEY (productID) REFERENCES Products(productID),
    FOREIGN KEY (customerID) REFERENCES Customer(customerID),
    FOREIGN KEY (orderDetailsID) REFERENCES OrderDetails(orderDetailsID)
);

GO

-- TRIGGER TỰ ĐỘNG SINH MÃ CHO ORDER - ĐÃ SỬA
CREATE OR ALTER TRIGGER trg_AutoGenerateOrderID
ON Orders
INSTEAD OF INSERT
AS
BEGIN
    DECLARE @newID VARCHAR(20), @maxID INT;
    SELECT @maxID = COALESCE(MAX(CAST(SUBSTRING(orderID, 4, 3) AS INT)), 0) + 1
    FROM Orders WITH (UPDLOCK, SERIALIZABLE);
    SET @newID = 'ORD' + RIGHT('000' + CAST(@maxID AS VARCHAR(3)), 3);
    
    -- Chèn TẤT CẢ các trường từ bảng inserted vào Orders
    INSERT INTO Orders (
        orderID, orderDate, totalAmount, customerID, employeeID, 
        orderStatus, paymentMethod, recipientName, recipientPhone,
        shippingAddress, shippingFee, notes
    )
    SELECT 
        @newID, i.orderDate, i.totalAmount, i.customerID, i.employeeID,
        i.orderStatus, i.paymentMethod, i.recipientName, i.recipientPhone,
        i.shippingAddress, i.shippingFee, i.notes
    FROM inserted i;
    
    -- Trả về ID mới được tạo để client có thể sử dụng
    SELECT @newID AS newOrderID;
END;
GO

-- TRIGGER TỰ ĐỘNG SINH MÃ CHO ORDERDETAILS
CREATE OR ALTER TRIGGER trg_AutoGenerateOrderDetailsID
ON OrderDetails
INSTEAD OF INSERT
AS
BEGIN
    DECLARE @newDetailID VARCHAR(20);
    DECLARE @currentMax INT;

    -- Cách tiếp cận an toàn hơn để sinh ID, tránh race condition so với COUNT(*)
    -- Tìm ID lớn nhất hiện tại và tăng lên 1
    SELECT @currentMax = ISNULL(MAX(CAST(SUBSTRING(orderDetailsID, 5, LEN(orderDetailsID) - 4) AS INT)), 0)
    FROM OrderDetails WITH (UPDLOCK, SERIALIZABLE); -- Khóa để đảm bảo tính nhất quán

    SET @newDetailID = 'ORDD' + RIGHT('000' + CAST(@currentMax + 1 AS VARCHAR(3)), 3);

    INSERT INTO OrderDetails (
        orderDetailsID,
        orderID,
        productID,
        quantity,
        unitPrice,    -- Đảm bảo cột này tồn tại trong bảng OrderDetails và trong `inserted`
        warrantyType,
        warrantyPrice,
        note
    )
    SELECT
        @newDetailID,
        i.orderID,
        i.productID,
        i.quantity,
        i.unitPrice,    -- Lấy từ dữ liệu được chèn
        i.warrantyType,
        i.warrantyPrice,
        i.note
    FROM inserted i;
END;
GO

-- TRIGGER TỰ ĐỘNG SINH MÃ CHO PRODUCT
CREATE OR ALTER TRIGGER before_insert_product
ON Products
INSTEAD OF INSERT
AS
BEGIN
    INSERT INTO Products (productID, productName, categoryID, description, price, priceCost, imagePath, quantity, createdAt,purchaseCount)
    SELECT
        c.categoryCode + RIGHT('000' + CAST(
            COALESCE(
                MAX(CAST(SUBSTRING(p.productID, LEN(c.categoryCode) + 1, LEN(p.productID)) AS INT)),
                100
            ) + ROW_NUMBER() OVER (PARTITION BY i.categoryID ORDER BY (SELECT NULL)) AS VARCHAR), 3),
        i.productName,
        i.categoryID,
        i.description,
        i.price,
        i.priceCost,
        i.imagePath,
        i.quantity,
        i.createdAt,
		i.purchaseCount
    FROM inserted i
    JOIN Categories c ON i.categoryID = c.categoryID
    LEFT JOIN Products p ON p.categoryID = i.categoryID AND p.productID LIKE c.categoryCode + '%'
    GROUP BY
        i.productName, i.categoryID, i.description, i.price, i.priceCost, i.imagePath, i.quantity, i.createdAt,i.purchaseCount, c.categoryCode;
END;
GO
IF NOT EXISTS (
  SELECT * FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_NAME = 'Categories' AND COLUMN_NAME = 'defaultWarrantyGroup'
)
BEGIN
  ALTER TABLE Categories ADD defaultWarrantyGroup INT DEFAULT 2;
END
GO
-- Trigger khi thêm chi tiết đơn hàng mới
CREATE OR ALTER TRIGGER trg_UpdateProductPurchaseCount
ON OrderDetails
AFTER INSERT
AS
BEGIN
    -- Cập nhật purchaseCount và giảm quantity khi thêm chi tiết đơn hàng
    UPDATE p
    SET p.purchaseCount = p.purchaseCount + i.quantity,
        p.quantity = p.quantity - i.quantity
    FROM Products p
    INNER JOIN inserted i ON p.productID = i.productID
    INNER JOIN Orders o ON i.orderID = o.orderID
    WHERE o.orderStatus = N'Đã xác nhận';
END;
GO

-- Trigger khi cập nhật trạng thái đơn hàng (từ "Đã xác nhận" sang "Đã hủy")
CREATE OR ALTER TRIGGER trg_AdjustProductCountOnOrderCancel
ON Orders
AFTER UPDATE
AS
BEGIN
    -- Nếu đơn hàng bị hủy, khôi phục lại số lượng sản phẩm và giảm purchaseCount
    IF EXISTS (
        SELECT * FROM inserted i
        JOIN deleted d ON i.orderID = d.orderID
        WHERE i.orderStatus = N'Đã hủy' AND d.orderStatus = N'Đã xác nhận'
    )
    BEGIN
        UPDATE p
        SET p.quantity = p.quantity + od.quantity,
            p.purchaseCount = p.purchaseCount - od.quantity
        FROM Products p
        JOIN OrderDetails od ON p.productID = od.productID
        JOIN inserted i ON od.orderID = i.orderID
        WHERE i.orderStatus = N'Đã hủy';
    END
END;
GO
