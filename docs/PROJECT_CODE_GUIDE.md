# CarRentalSystem - Code Guide chi tiet

Tai lieu nay giai thich cau truc source code, cac file quan trong, cac ham chinh va luong nghiep vu cua du an.

Muc tieu cua file nay:

- Giup nguoi moi clone project hieu duoc moi package dang lam gi.
- Giai thich tung nhom file theo luong MVC: request -> servlet -> service/DAO -> database -> JSP.
- Ghi ro cac tinh nang chinh: tim xe, dat xe, thanh toan PayOS, webhook, quyet toan, hoan tien, ho tro, admin/staff/manager/driver.
- Lam tai lieu mentor de khi bao ve hoac refactor tiep co the tra cuu nhanh.

Tai lieu nay giai thich theo muc file, class, ham va luong xu ly. Neu can giai thich tung dong mot cua mot file cu the, nen tach thanh tai lieu rieng cho file do, vi du `docs/payment-dao-line-by-line.md`.

## 1. Tong quan kien truc

Du an la ung dung Java Web MVC:

```text
Browser
  -> Servlet Controller
  -> Service/DAO
  -> SQL Server
  -> Model object
  -> JSP view
  -> Browser
```

Cong nghe chinh:

- Java 17.
- Jakarta Servlet 6, JSP 3.
- JSTL cho JSP.
- SQL Server qua JDBC.
- Tomcat 10.1.
- PayOS cho thanh toan QR.
- ZXing cho QR fallback/QR thu cong.

Thu muc chinh:

```text
src/main/java/com/carrental/config      Cau hinh ket noi DB
src/main/java/com/carrental/controller  Servlet controller
src/main/java/com/carrental/dao         Truy van DB va workflow nghiep vu du lieu
src/main/java/com/carrental/filter      Filter request
src/main/java/com/carrental/model       DTO/entity model
src/main/java/com/carrental/service     Service tich hop PayOS, checkout, QR
src/main/webapp                         JSP, CSS, JS, web.xml
sql                                     Schema, seed, migration
docs                                    Tai lieu du an
```

## 2. Luong request tong quat

Vi du nguoi dung vao `/search`:

1. Browser goi `GET /CarRentalSystem/search`.
2. Tomcat tim servlet co `@WebServlet("/search")`, la `CarSearchServlet`.
3. Servlet doc query param nhu ngay nhan xe, ngay tra xe, so ghe.
4. Servlet goi `CarDAO` de lay danh sach xe hoac nhom xe kha dung.
5. Servlet set attribute vao request.
6. Servlet forward sang JSP `WEB-INF/views/search.jsp`.
7. JSP dung JSTL de render HTML.

Vi du nguoi dung dat xe:

1. `/search` chon xe.
2. `/book` hien trang xac nhan dat xe.
3. `BookingServlet.doPost` tao hop dong qua `CheckoutService`.
4. `CheckoutService` goi `ContractDAO.createPendingBookingWithDetailsAndPayments`.
5. `ContractDAO` tao `Contracts`, `Contract_Details`, goi `PaymentDAO.createPendingPayments`.
6. `PaymentDAO` delegate sang `PaymentGatewayWorkflow`.
7. He thong tao link PayOS va luu `Payment_Transactions`, `Payments`.
8. User duoc dua sang trang `payment-pending.jsp` hoac QR.
9. PayOS webhook goi `/payment/webhook/payos`.
10. He thong verify signature, mark payment paid, cap nhat contract.

## 3. Trang thai nghiep vu quan trong

### 3.1 Contract status

`ContractStatus.java` gom cac trang thai hop dong:

- `PENDING_PAYMENT`: hop dong da tao nhung chua thanh toan coc.
- `PAYMENT_EXPIRED`: het han thanh toan.
- `RESERVED`: da thanh toan coc, giu xe.
- `CONFIRMED`: staff xac nhan don.
- `CAR_DELIVERED`: da giao xe.
- `CAR_RETURNED`: khach da tra xe.
- `SETTLEMENT_PENDING`: dang cho quyet toan/hoan coc.
- `COMPLETED`: hoan tat.
- `CANCELLED`: da huy.

### 3.2 Payment status

`PaymentStatus.java` gom:

- `PENDING`: dang doi thanh toan.
- `PAID`: da thanh toan.
- `FAILED`: loi thanh toan.
- `EXPIRED`: het han.
- `REFUND_PENDING`: dang cho hoan tien.
- `REFUNDED`: da hoan tien.
- `PARTIALLY_REFUNDED`: hoan mot phan.

### 3.3 Payment type

`PaymentType.java` gom:

- `DEPOSIT`: tien coc giu xe.
- `RENTAL_PREPAID`: tien thue tra truoc.
- `DRIVER_FEE_PREPAID`: phi tai xe tra truoc.
- `RENTAL_BALANCE`: so tien con lai can thanh toan khi quyet toan.
- `EXTRA_CHARGE`: phu phi sau khi tra xe.
- `REFUND`: dong ghi nhan hoan tien.

## 4. File cau hinh

### `pom.xml`

Muc dich:

- Khai bao project Maven WAR.
- Dat Java source/target la 17.
- Khai bao dependencies:
  - Servlet/JSP/JSTL cho Tomcat 10.1.
  - SQL Server JDBC.
  - ZXing de tao QR.
  - `org.json` de parse JSON PayOS.
- Dat final WAR name la `CarRentalSystem`.

Lien quan:

- NetBeans dung file nay de build.
- Tomcat deploy WAR/context theo ten `CarRentalSystem`.

### `src/main/webapp/WEB-INF/web.xml`

Muc dich:

- Cau hinh web app Jakarta Servlet 6.
- Dat session timeout 30 phut.
- Dat welcome page la `index.jsp`.
- Khai bao `CharacterEncodingFilter` cho tat ca request.
- Khai bao error page cho 403/404/500.

Chu y:

- Servlet mapping chu yeu dung annotation `@WebServlet`, khong khai bao trong web.xml.

### `src/main/webapp/META-INF/context.xml`

Muc dich:

- File context cua webapp khi deploy tren Tomcat.
- Neu can cau hinh context path hoac resource Tomcat thi dat o day.

### `src/main/java/com/carrental/config/DBContext.java`

Muc dich:

- Tao connection den SQL Server.
- La diem tap trung de sua URL, username, password.

Ham chinh:

- `getConnection()`: tra ve `java.sql.Connection` moi.

Lien quan:

- Moi DAO deu goi `DBContext.getConnection()`.
- Neu clone sang may khac bi loi DB, file nay la diem can kiem tra dau tien.

## 5. Filters

### `CharacterEncodingFilter.java`

Muc dich:

- Dat encoding UTF-8 cho request va response.
- Giam loi tieng Viet bi loi font khi submit form.

Ham chinh:

- `doFilter`: set encoding roi goi `chain.doFilter`.

Lien quan:

- Duoc map `/*` trong `web.xml`.

### `AuthFilter.java`

Muc dich:

- Chan truy cap cac route can dang nhap.
- Kiem tra session co user hay khong.
- Kiem tra role voi cac route admin/staff/manager/driver/customer.

Ham chinh:

- `doFilter`: doc URL, session, role; neu khong hop le thi redirect login hoac tra 403.

Lien quan:

- Cac servlet van co the tu check session rieng, nhung filter la lop bao ve chung.

## 6. Controllers

Controller la lop nhan HTTP request. Thuong co:

- `doGet`: hien trang, load data.
- `doPost`: xu ly form/action.
- Goi DAO/service.
- Forward sang JSP hoac redirect.

### `LoginServlet.java` - route `/login`

Muc dich:

- Hien form dang nhap.
- Xu ly dang nhap bang username/password.

Ham:

- `doGet`: forward sang `login.jsp`.
- `doPost`: goi `UserDAO.login`, luu `user` va `roles` vao session, redirect theo role.

Lien quan:

- `UserDAO.login`.
- `AuthFilter`.
- Views: `login.jsp`.

### `LogoutServlet.java` - route `/logout`

Muc dich:

- Dang xuat user.

Ham:

- `doGet`: invalidate session, redirect ve login.

### `RegisterServlet.java` - route `/register`

Muc dich:

- Dang ky tai khoan customer.

Ham:

- `doGet`: hien form dang ky.
- `doPost`: validate username/email, tao user customer qua `UserDAO.registerCustomer`.

Lien quan:

- `UserDAO.usernameExists`.
- `UserDAO.emailExists`.
- `UserDAO.registerCustomer`.

### `ProfileServlet.java` - route `/profile`

Muc dich:

- Hien va cap nhat thong tin ca nhan.
- Cho customer cap nhat thong tin ngan hang hoan tien.

Ham:

- `doGet`: lay user tu session, load lai tu DB, forward `profile.jsp`.
- `doPost`: cap nhat ho so bang `UserDAO.updateCustomerProfile`.

Lien quan:

- `User.hasRefundBankInfo`.
- `User.bankCode`, `bankName`, `bankAccountNumber`, `bankAccountHolder`.

### `CarSearchServlet.java` - route `/search`

Muc dich:

- Trang tim xe theo thoi gian, so ghe, hang xe, kieu hop so.

Ham:

- `doGet`: hien form tim kiem va danh sach neu co param.
- `doPost`: redirect ve GET de URL co query param.

Lien quan:

- `CarDAO.findAvailableCarGroups`.
- View: `search.jsp`.

### `CarCatalogServlet.java` - route `/cars`

Muc dich:

- Hien danh sach xe/dong xe cho nguoi dung lua chon.

Ham:

- `doGet`: load catalog, brand, seat count, render `car-catalog.jsp`.

Lien quan:

- `CarDAO.getCatalogCarGroups`.
- `CarDAO.getAvailableBrands`.
- `CarDAO.getAvailableSeatCounts`.

### `CarSelectServlet.java` - route `/select-car`

Muc dich:

- Chon xe hoac bo chon xe trong session truoc khi dat.

Ham:

- `doGet`: doc `carId`, selection mode, pickup/return; cap nhat selected cars trong session; redirect ve trang phu hop.

Lien quan:

- Session attribute danh sach xe da chon.

### `BookingServlet.java` - route `/book`

Muc dich:

- Hien trang xac nhan dat xe.
- Tao hop dong va payment pending khi user submit.

Ham:

- `doGet`: load xe da chon, tai xe kha dung, tinh tien tam thoi, forward `booking.jsp`.
- `doPost`: doc form pickup location, payment mode, driver option; goi `CheckoutService.processCheckout`; redirect sang payment pending/QR.

Lien quan:

- `CheckoutService`.
- `CarDAO`.
- `DriverDAO`.
- `PaymentMode`.
- View: `booking.jsp`, JS: `booking.js`.

### `PaymentPendingServlet.java` - route `/payment/pending`

Muc dich:

- Hien trang cho thanh toan QR.
- Reconcile nhanh trang thai payment khi user quay lai.

Ham:

- `doGet`: lay transaction ref, goi `PaymentDAO.reconcilePendingTransactionWithGateway`, forward `payment-pending.jsp`.

Lien quan:

- `PaymentDAO`.
- `PayOsGateway`.

### `PaymentQrServlet.java` - route `/payment/qr`

Muc dich:

- Tra ve QR image hoac QR data cho giao dich thanh toan.

Ham:

- `doGet`: tim transaction, doc QR payload/provider QR, render image/redirect data tuy implementation.

Lien quan:

- `PaymentDAO.getTransactionByRef`.
- ZXing.

### `PaymentStatusServlet.java` - route `/payment/status`

Muc dich:

- Endpoint cho frontend poll trang thai thanh toan.

Ham:

- `doGet`: lay transaction ref, reconcile voi PayOS neu can, tra JSON trang thai.

Lien quan:

- `PaymentDAO.reconcilePendingTransactionWithGateway`.

### `PayOsWebhookServlet.java` - route `/payment/webhook/payos`

Muc dich:

- Nhan webhook tu PayOS.
- Verify signature.
- Cap nhat transaction/payment/contract.

Ham:

- `doPost`: doc JSON body, verify PayOS signature, goi `PaymentDAO.confirmPaymentFromGatewayWebhook`.

Lien quan:

- `PayOsGateway.verifyWebhook`.
- `PaymentWebhookResult`.
- `PaymentGatewayWorkflow`.

### `CustomerContractsServlet.java` - route `/my-contracts`

Muc dich:

- Hien hop dong cua customer.
- Cho customer huy hop dong neu du dieu kien.

Ham:

- `doGet`: load contracts cua customer.
- `doPost`: xu ly action huy hop dong, tao refund pending neu can.

Lien quan:

- `ContractDAO.getContractsByCustomerId`.
- `PaymentDAO.cancelContractWithRefund`.

### `ContractDetailServlet.java` - route `/contract-detail`

Muc dich:

- Xem chi tiet hop dong.
- Hien payment record, refund, QR hoan tien thu cong neu co.

Ham:

- `doGet`: load contract, details, payment records, refund, customer bank info.

Lien quan:

- `ContractDAO`.
- `PaymentDAO.getPaymentRecordsByContractId`.
- `PaymentDAO.getPendingRefundByContractId`.

### `StaffDashboardServlet.java` - route `/staff/dashboard`

Muc dich:

- Dashboard staff xu ly hop dong.

Ham:

- `doGet`: load danh sach hop dong theo status, hien cac action.

Lien quan:

- `ContractDAO.getAllContracts`.
- View: `staff-dashboard.jsp`.

### `ContractProcessServlet.java` - route `/staff/process`

Muc dich:

- Staff cap nhat tien trinh hop dong.

Ham:

- `doPost`: doc action/status moi, goi `ContractDAO.updateContractStatus`.

Lien quan:

- Status flow: confirm, deliver, return, cancel.

### `SettlementServlet.java` - route `/staff/settlement`

Muc dich:

- Staff quyet toan sau khi xe tra ve.
- Tao link thanh toan so du neu khach con thieu tien.
- Tao yeu cau hoan coc neu can hoan tien.
- Mark refund completed khi da chuyen/tra tien thu cong.

Ham:

- `doGet`: tinh settlement, load pending balance transaction, load refund.
- `doPost`: xu ly action tao link balance, ghi nhan thanh toan tien mat, tao refund, xac nhan refund.

Lien quan:

- `PaymentDAO.calculateSettlement`.
- `PaymentDAO.createRentalBalancePaymentLink`.
- `PaymentDAO.recordRentalBalancePayment`.
- `PaymentDAO.createRefundRequest`.
- `PaymentDAO.markRefundCompleted`.

### `DriverScheduleServlet.java` - route `/driver/schedule`

Muc dich:

- Tai xe xem lich duoc phan cong.
- Tai xe cap nhat trang thai assignment.

Ham:

- `doGet`: load lich theo driver.
- `doPost`: update assignment status.

Lien quan:

- `DriverDAO.getScheduleByDriverId`.
- `DriverDAO.updateAssignmentStatus`.

### `ManagerDashboardServlet.java` - route `/manager/dashboard`

Muc dich:

- Manager quan ly xe, tai xe, dashboard tong quan.

Ham:

- `doGet`: load cars/drivers/contracts tuy view.
- `doPost`: xu ly them/sua xe hoac action quan ly.

Lien quan:

- `CarDAO`.
- `DriverDAO`.

### `AdminDashboardServlet.java` - route `/admin/dashboard`

Muc dich:

- Admin quan ly user, role, status, thong tin ngan hang cua nguoi khac.

Ham:

- `doGet`: load users, roles.
- `doPost`: create/update user, update roles, update status.

Lien quan:

- `UserDAO.getAllUsers`.
- `UserDAO.createUser`.
- `UserDAO.updateUserByAdmin`.
- `UserDAO.updateUserRoles`.

### `SupportServlet.java` - route `/support`

Muc dich:

- Customer tao ticket ho tro.
- Customer xem lich su ticket cua minh.

Ham:

- `doGet`: load ticket cua user hien tai.
- `doPost`: validate category/status/priority, tao ticket.

Lien quan:

- `SupportTicketDAO`.
- View: `support.jsp`.

### `StaffSupportServlet.java` - route `/staff/support`

Muc dich:

- Staff xem va phan hoi ticket ho tro.

Ham:

- `doGet`: load all ticket theo filter.
- `doPost`: cap nhat status, priority, staff response.

Lien quan:

- `SupportTicketDAO.getAllTickets`.
- `SupportTicketDAO.updateTicketByStaff`.

## 7. DAO va workflow

### `CarDAO.java`

Muc dich:

- Doc/ghi du lieu xe.
- Tim xe kha dung theo khoang thoi gian.
- Gom xe theo brand/model/type de hien catalog.

Ham chinh:

- `findAvailableCars`: tim xe con trong khoang pickup/return.
- `findAvailableCarGroups`: gom xe thanh nhom de nguoi dung chon theo dong xe.
- `findSpecificAvailableCarsByGroup`: lay xe cu the trong nhom.
- `getCarById`: lay chi tiet xe.
- `getCatalogCarGroups`: lay danh sach xe cho trang `/cars`.
- `getAllCars`: manager/admin xem tat ca xe.
- `insertCar`, `updateCar`: them/sua xe.
- `findRandomAvailableCarInSameGroup`: khi user chon group, he thong lay mot xe cu the kha dung.
- `findAvailableCarById`: kiem tra mot xe cu the co kha dung trong thoi gian chon khong.
- `getAllCarTypes`, `getAvailableBrands`, `getAvailableSeatCounts`: du lieu filter.

### `ContractDAO.java`

Muc dich:

- Tao hop dong va chi tiet hop dong.
- Lay danh sach hop dong theo customer/status.
- Cap nhat trang thai hop dong.

Ham chinh:

- `createContractWithDetails`: tao contract co details khong kem payment.
- `createPendingBookingWithDetailsAndPayments`: tao contract, contract details, driver assignment, pending payment trong mot transaction DB.
- `getContractsByStatus`, `getAllContracts`, `getContractsByCustomerId`: list contract.
- `getContractById`: lay header contract.
- `getDetailsByContractId`: lay cac xe trong contract.
- `updateContractStatus`: staff chuyen status.

Lien quan:

- Goi `PaymentDAO.createPendingPayments` khi tao booking can thanh toan.
- Goi `DriverDAO.assignDriver`/insert assignment neu user thue tai xe.

### `DriverDAO.java`

Muc dich:

- Quan ly tai xe va lich phan cong.

Ham chinh:

- `getActiveDrivers`: lay tai xe active.
- `getDriversWithAvailability`: lay tai xe chua trung lich trong pickup/return.
- `getActiveAssignments`: staff/manager xem assignment dang active.
- `getAssignmentByDetailId`: lay assignment theo contract detail.
- `assignDriver`: gan driver cho contract detail.
- `getScheduleByDriverId`: driver xem lich cua minh.
- `updateAssignmentStatus`: driver/staff cap nhat trang thai assignment.

### `UserDAO.java`

Muc dich:

- Dang nhap, dang ky, quan ly user/role/profile.

Ham chinh:

- `login`: xac thuc username/password.
- `registerCustomer`: tao user customer va record customer.
- `getCustomerIdByUserId`: map user sang customer.
- `getDriverIdByUserId`: map user sang driver.
- `getUserRoles`: lay roles cua user.
- `getAllUsers`, `getUserById`, `getUserByCustomerId`: load user.
- `updateCustomerProfile`: customer sua ho so va bank info.
- `updateUserByAdmin`: admin sua thong tin user, gom ca bank info.
- `createUser`: admin tao user va gan roles.
- `updateUserStatus`: active/inactive/locked.
- `updateUserRoles`: cap nhat role.
- `getAllRoleNames`, `usernameExists`, `emailExists`: helper validate.

### `SupportTicketDAO.java`

Muc dich:

- CRUD ticket ho tro.

Ham chinh:

- `getTicketsByUserId`: customer xem ticket cua minh.
- `getAllTickets`: staff xem tat ca ticket, co filter status.
- `createTicket`: tao ticket moi.
- `updateTicketByStaff`: staff cap nhat phan hoi/trang thai.
- `isValidCategory`, `isValidStatus`, `isValidPriority`: validate input tu form.

### `PaymentDAO.java`

Muc dich:

- Facade giu API cu cho controller/DAO khac.
- Sau refactor, file nay khong con chua SQL lon, chi delegate sang cac workflow/store.

Ham public:

- `createPendingPayments`: delegate `PaymentGatewayWorkflow.createPendingPayments`.
- `getTransactionByRef`: delegate gateway workflow.
- `reconcilePendingTransactionWithGateway`: doi soat PayOS.
- `getLatestPendingTransactionByContractId`: lay transaction pending moi nhat.
- `confirmPaymentTransaction`: confirm thu cong/return flow.
- `confirmPaymentFromGatewayWebhook`: xu ly webhook PayOS.
- `calculateSettlement`: tinh tien can thu/hoan.
- `createRefundRequest`: tao yeu cau hoan tien.
- `markRefundCompleted`: staff xac nhan da hoan.
- `markRefundGatewayFailed`: luu loi gateway refund.
- `getPendingRefundByContractId`, `getRefundById`: query refund.
- `getLatestPendingBalanceTransactionByContractId`: lay link thanh toan so du.
- `createRentalBalancePaymentLink`: tao QR thanh toan so du.
- `getPaymentRecordsByContractId`: lay lich su payment.
- `recordRentalBalancePayment`: ghi nhan tien mat.
- `calculateCancellationRefundAmount`: tinh tien hoan khi huy.
- `cancelContractWithRefund`: huy hop dong va tao refund neu can.

### `PaymentGatewayWorkflow.java`

Muc dich:

- Workflow lien quan PayOS va transaction thanh toan online.

Ham chinh:

- `createPendingPayments`: tinh so tien can thanh toan, tao PayOS link, insert transaction, insert payment lines.
- `getTransactionByRef`: tim transaction theo provider ref.
- `reconcilePendingTransactionWithGateway`: query PayOS de xem giao dich pending da paid chua.
- `getLatestPendingTransactionByContractId`: lay pending transaction moi nhat cua contract.
- `confirmPaymentTransaction`: confirm transaction bang provider ref.
- `confirmPaymentFromGatewayWebhook`: xu ly webhook PayOS, chong duplicate, verify amount/status, mark paid.
- `confirmPaidTransactionFromGatewayStatus`: private helper cho reconcile.
- `applyPaidTransaction`: mark transaction paid, mark payment lines paid, reserve contract neu co coc.

### `PaymentGatewayLinkService.java`

Muc dich:

- Wrapper goi `PayOsGateway.createPaymentLink`.
- Chuyen exception PayOS thanh `SQLException` de transaction DB co the rollback.

Ham:

- `createPaymentLink`: goi gateway, neu interrupted thi set lai interrupt flag.

### `PaymentWebhookEventStore.java`

Muc dich:

- Thao tac bang `Payment_Webhook_Events`.

Ham:

- `insert`: luu webhook event, tra ve null neu duplicate key.
- `markProcessed`: mark PROCESSED.
- `markFailed`: mark FAILED va luu error message.

### `PaymentTransactionStore.java`

Muc dich:

- Thao tac bang `Payment_Transactions` va cac payment line pending.

Ham:

- `insert`: insert transaction PayOS pending.
- `insertPaymentLine`: insert dong `Payments` lien quan transaction.
- `findLatestPendingByPaymentType`: tim pending transaction theo payment type.
- `findByProviderRef`: tim transaction theo provider ref.
- `findLatestPendingByContractId`: tim pending moi nhat cua contract.
- `isExpired`: check expiredAt.
- `lockByProviderRef`: select with `UPDLOCK, ROWLOCK`.
- `lockByProviderOrderCode`: lock transaction theo provider/order code.

### `PaymentWorkflowUpdates.java`

Muc dich:

- Gom cac cau update trang thai payment/contract.

Ham:

- `markTransactionPaid`: set transaction PAID.
- `markPaymentLinesPaid`: set payment lines PAID.
- `hasPaidDeposit`: check transaction co dong DEPOSIT paid.
- `reserveContractAfterDeposit`: chuyen contract PENDING_PAYMENT -> RESERVED.
- `markFinalPaidIfPrepaid`: set FinalPaidAt neu da tra du.
- `expireTransaction`: expire transaction, payment lines, contract pending payment.
- `markDepositRefundPending`: mark source payment REFUND_PENDING.
- `moveContractToSettlementPending`: CAR_RETURNED -> SETTLEMENT_PENDING.
- `tryCompleteSettledContract`: CAR_RETURNED/SETTLEMENT_PENDING -> COMPLETED.
- `getContractStatus`, `getContractStatusForUpdate`: query status.
- `markCancellationPaymentsRefundPending`: khi huy, mark payment da thu sang refund pending.
- `updateCancelledContractPaymentsAfterRefund`: sau khi hoan, mark refunded.
- `cancelContract`: huy contract, detail, pending payment.

### `PaymentSettlementCalculator.java`

Muc dich:

- Tinh quyet toan sau khi tra xe.

Logic:

1. Load `FinalAmountDue`.
2. Tinh tong coc da thu.
3. Tinh tong da hoan.
4. Tinh tien thue/tai xe da thanh toan.
5. Tinh extra charge chua thanh toan.
6. Dung coc de tru mot phan extra charge neu co.
7. Tinh `amountToCollect`.
8. Tinh `refundAmount`.
9. Tim `sourcePaymentId` cua coc de tao refund.

Ham:

- `calculate`: public static trong package, tra `SettlementResult`.
- `loadContractAmount`: lay FinalAmountDue.
- `sumPayments`: helper SUM Amount theo filter.

### `PaymentRefundWorkflow.java`

Muc dich:

- Workflow hoan tien va huy hop dong.

Ham:

- `createRefundRequest`: tao refund pending tu settlement.
- `markRefundCompleted`: mark refund refunded, insert payment line refund, cap nhat source payment, complete contract neu du dieu kien.
- `markRefundGatewayFailed`: luu loi khi gateway refund fail.
- `getPendingRefundByContractId`: lay refund pending.
- `getRefundById`: lay refund theo id.
- `calculateCancellationRefundAmount`: tinh tien hoan khi huy.
- `cancelContractWithRefund`: huy hop dong, tao refund pending neu hop dong da thu tien.

### `PaymentRefundStore.java`

Muc dich:

- Store query/update lien quan bang `Refunds` va payment refund.

Ham:

- `sumRefundedAmount`: tong tien da hoan.
- `insert`: insert refund pending.
- `lock`: lock refund theo id.
- `markCompleted`: update refund sang REFUNDED.
- `insertRefundPaymentLine`: insert dong `Payments` type REFUND.
- `updateSourcePaymentAfterRefund`: mark source payment REFUNDED/PARTIALLY_REFUNDED.
- `markGatewayFailed`: luu loi gateway refund.
- `findPending`, `findById`: query refund.
- `calculateCancellationRefundAmount`: tinh so tien co the hoan khi huy.
- `findLatestRefundablePaymentId`: tim payment source khi huy.
- `findLatestDepositPaymentId`: tim payment coc source khi quyet toan.

### `PaymentBalanceWorkflow.java`

Muc dich:

- Workflow thanh toan phan tien con lai khi quyet toan.

Ham:

- `getLatestPendingBalanceTransactionByContractId`: tim pending balance QR.
- `createRentalBalancePaymentLink`: tao PayOS QR cho so tien con lai.
- `getPaymentRecordsByContractId`: lay lich su payment cua contract.
- `recordRentalBalancePayment`: staff ghi nhan thu tien mat.

### `PaymentRowMapper.java`

Muc dich:

- Chuyen `ResultSet` sang object model.

Ham:

- `mapTransaction`: map sang `PaymentTransaction`.
- `mapPaymentRecord`: map sang `PaymentRecord`.
- `mapRefund`: map sang `Refund`.

Ly do tach:

- Giam duplicate code trong DAO.
- Neu DB column doi, sua mapper o mot cho.

### `PaymentAmounts.java`

Muc dich:

- Helper tinh tien an toan voi `BigDecimal`.

Ham:

- `safe`: null -> zero.
- `maxZero`: neu am thi tra zero.
- `min`: lay gia tri nho hon.

### `PaymentOrderCodeGenerator.java`

Muc dich:

- Tao order code cho PayOS tu contractId + timestamp entropy + random.

Ham:

- `generate`: tao long order code.

### `PaymentGatewayLinkService.java`

Muc dich:

- Tach phan goi PayOS create payment link ra khoi workflow de de test/refactor.

## 8. Services

### `CheckoutService.java`

Muc dich:

- Service dieu phoi luong checkout tu `BookingServlet`.

Ham:

- `processCheckout`: validate customer, xe, pickup/return, tinh tien, tao contract details, goi `ContractDAO`.

Lien quan:

- `CheckoutResult`.
- `ContractDAO`.
- `PaymentDAO`.

### `PayOsGateway.java`

Muc dich:

- Tich hop API PayOS.
- Doc config PayOS tu env/system/local config.
- Tao payment link.
- Verify webhook.
- Query payment status.

Ham:

- constructor: load config.
- `isConfigured`: kiem tra co client id/api key/checksum key/base url.
- `createPaymentLink`: tao request PayOS, ky checksum, parse response.
- `verifyWebhook`: verify HMAC signature tu PayOS.
- `getPaymentLinkInformation`: query status theo order code.

Lien quan:

- `PaymentGatewayWorkflow`.
- `PayOsWebhookServlet`.

### `PaymentLinkRequest.java`

DTO input khi tao PayOS link:

- `orderCode`.
- `contractCode`.
- `amount`.
- `expiredAt`.

### `PaymentLinkResponse.java`

DTO output khi PayOS tao link:

- `provider`.
- `paymentLinkId`.
- `checkoutUrl`.
- `qrCode`.
- `rawResponse`.

### `PaymentLinkStatusResponse.java`

DTO output khi query status:

- `orderCode`.
- `amount`.
- `amountPaid`.
- `amountRemaining`.
- `status`.
- `providerPaymentRef`.
- `rawResponse`.

### `RefundQrBuilder.java`

Muc dich:

- Tao URL QR thu cong cho hoan tien dua tren thong tin ngan hang nguoi nhan.

Ham:

- `buildRefundQrUrl`: nhan user, amount, addInfo va tao URL QR.

## 9. Models

Model chu yeu la JavaBean: field private, getter/setter public, dung de chuyen du lieu giua DAO, servlet va JSP.

### Xe va catalog

- `Car.java`: xe cu the, gom bien so, brand, model, rate, deposit, image, status, type.
- `CarGroup.java`: nhom xe theo brand/model/type de hien catalog co so luong kha dung.
- `CarType.java`: loai xe, so ghe, mo ta.

### Hop dong

- `Contract.java`: header hop dong, customer, status, pickup/return, tong tien.
- `ContractDetail.java`: tung xe trong hop dong, gia, phi tai xe, status detail.
- `ContractStatus.java`: enum/string constants trang thai contract.
- `CheckoutResult.java`: ket qua checkout, gom success/error/payment transaction/contract.

### User va role

- `User.java`: tai khoan dang nhap, profile, bank info.
  - `hasRefundBankInfo`: kiem tra du thong tin ngan hang de hoan tien.
  - `getBankInfoLocked`: dung UI khoa/chinh sua bank info neu can.
- `Customer.java`: thong tin customer, license.
- `Driver.java`: thong tin tai xe.
- `DriverAssignment.java`: lich tai xe duoc phan cong.
- `Role.java`: role system.

### Payment/refund

- `Payment.java`: record payment co ban.
- `PaymentRecord.java`: view model lich su payment, co `getDisplayMethod`, `getDisplayType`.
- `PaymentTransaction.java`: transaction voi PayOS.
- `PaymentMode.java`: `DEPOSIT_ONLY` hoac `FULL_PREPAYMENT`.
- `PaymentStatus.java`: status payment/refund.
- `PaymentType.java`: type payment line.
- `PaymentWebhookResult.java`: ket qua xu ly webhook, co success/duplicate/failure.
- `Refund.java`: thong tin refund.
- `RefundMethod.java`: `GATEWAY_REFUND`, `MANUAL_BANK_TRANSFER`, `CASH_AT_COUNTER`, `WALLET_CREDIT`.
- `SettlementResult.java`: ket qua quyet toan, safe BigDecimal de tranh null.

### Support

- `SupportTicket.java`: ticket ho tro.
  - `getCategoryLabel`: hien label tieng Viet.
  - `getStatusLabel`: hien label trang thai.
  - `getPriorityLabel`: hien label muc do uu tien.

## 10. JSP, CSS, JS

### Includes

- `WEB-INF/includes/header.jsp`: navbar, link dieu huong theo role/session.
- `WEB-INF/includes/footer.jsp`: footer chung.

### Views

- `login.jsp`: form dang nhap.
- `register.jsp`: form dang ky customer.
- `profile.jsp`: ho so user, thong tin ngan hang refund.
- `search.jsp`: tim xe theo ngay, so ghe, filter.
- `car-catalog.jsp`: xem danh sach xe/dong xe.
- `select-car.jsp`: trang chon xe cu the trong group.
- `booking.jsp`: xac nhan dat xe, pickup location, thue tai xe, payment mode.
- `payment-pending.jsp`: trang cho thanh toan QR/checkout.
- `my-contracts.jsp`: danh sach hop dong customer.
- `contract-detail.jsp`: chi tiet hop dong, payment record, refund.
- `staff-dashboard.jsp`: staff xu ly hop dong.
- `settlement.jsp`: staff quyet toan, tao QR thanh toan so du, hoan coc.
- `driver-schedule.jsp`: lich tai xe.
- `manager-dashboard.jsp`: manager quan ly xe/tai xe.
- `admin-dashboard.jsp`: admin quan ly user/role/bank info.
- `support.jsp`: customer tao/xem ticket.
- `staff-support.jsp`: staff xu ly ticket.
- `error.jsp`: trang loi chung.

### `js/booking.js`

Muc dich:

- Dieu khien frontend trang booking.
- Load tinh/thanh, quan/huyen, phuong/xa tu API cong khai.
- Co fallback location neu API fail.
- Cap nhat hidden input `pickupLocation`.
- Cap nhat realtime tong tien khi user tick thue tai xe hoac doi payment mode.

Ham:

- `selectedValue`: lay gia tri da chon ban dau tu `data-selected`.
- `normalizeLocations`: chuyen response API tinh thanh ve format noi bo.
- `ensureOther`: them lua chon "Khac".
- `setOptions`: fill option cho select.
- `findByName`: tim province/district theo name.
- `updatePickupLocationValue`: ghep dia chi chi tiet + ward + district + province.
- `initLocationSelectors`: khoi tao cascading select.
- `parseAmount`: parse amount tu dataset.
- `formatVnd`: format VND.
- `updateBookingSummary`: tinh driver fee, full prepayment, required payment.

### `css/style.css`

Muc dich:

- Style chung cho UI.
- Card xe, dashboard, form, button, responsive layout.

Chu y:

- Phan xe tren mobile can giu layout khong de button de len gia.
- Nen test lai responsive sau khi sua CSS.

## 11. SQL files

- `database-schema.sql`: tao bang chinh, constraints, index.
- `seed-demo-data.sql`: tai khoan, role, data demo.
- `seed-payos-test-cars.sql`: xe test PayOS.
- `seed-fleet-quantity-demo-cars.sql`: data xe co anh online.
- `payment-refactor-migration.sql`: migration cho payment transaction/payment line.
- `payment-gateway-refund-migration.sql`: migration lien quan refund gateway/manual.
- `customer-bank-refund-migration.sql`: them bank info cho customer/user.
- `support-ticket-migration.sql`: tao bang support ticket.
- `check-payment-refactor.sql`: kiem tra schema payment sau refactor.
- `diagnose-sql-connection.sql`: ho tro debug SQL connection.
- `test-data.sql`: data test cu.
- `enable-tcp.ps1`: helper bat TCP/IP SQL Server tren Windows.

## 12. Luong tinh nang chi tiet

### 12.1 Tim xe

1. User vao `/search`.
2. `CarSearchServlet` doc ngay gio va filter.
3. `CarDAO.findAvailableCarGroups` query xe active, loai xe, contract detail dang overlap.
4. JSP hien group xe con kha dung.
5. User chon group hoac xe cu the.

### 12.2 Dat xe

1. User chon xe va vao `/book`.
2. `BookingServlet.doGet` lay xe da chon va tinh tam tien.
3. `booking.js` cap nhat dia chi/tai xe/tong tien realtime.
4. User submit.
5. `CheckoutService.processCheckout` validate va tao checkout.
6. `ContractDAO.createPendingBookingWithDetailsAndPayments` tao DB transaction.
7. `PaymentDAO.createPendingPayments` delegate `PaymentGatewayWorkflow`.
8. He thong tao PayOS QR.

### 12.3 Thanh toan PayOS

1. `PaymentGatewayWorkflow.createPendingPayments` tao order code.
2. `PaymentGatewayLinkService.createPaymentLink` goi PayOS.
3. `PaymentTransactionStore.insert` luu transaction pending.
4. `PaymentTransactionStore.insertPaymentLine` luu cac dong payment pending.
5. User thanh toan QR.
6. PayOS webhook goi `PayOsWebhookServlet`.
7. `PayOsGateway.verifyWebhook` verify signature.
8. `PaymentGatewayWorkflow.confirmPaymentFromGatewayWebhook` lock transaction.
9. Neu amount/status hop le, mark transaction va payment lines paid.
10. Neu co deposit paid, contract chuyen `RESERVED`.

### 12.4 Quyet toan sau khi tra xe

1. Staff vao settlement.
2. `PaymentSettlementCalculator.calculate` tinh:
   - Coc da thu.
   - Rental/driver fee da thu.
   - Extra charge chua thanh toan.
   - So tien can thu them.
   - So tien hoan coc.
3. Neu con thieu, staff tao QR thanh toan so du qua `PaymentBalanceWorkflow`.
4. Neu co tien hoan, staff tao refund pending qua `PaymentRefundWorkflow`.
5. Staff hoan tien thu cong/QR va mark completed.
6. Contract chuyen `COMPLETED` neu du dieu kien.

### 12.5 Ho tro khach hang

1. Customer vao `/support`.
2. Tao ticket voi category/subject/message.
3. `SupportTicketDAO.createTicket` luu ticket.
4. Staff vao `/staff/support`, phan hoi va cap nhat status.
5. Customer thay phan hoi trong trang support.

### 12.6 Admin quan ly user

1. Admin vao `/admin/dashboard`.
2. Xem user, role, status.
3. Tao/sua user.
4. Cap nhat bank info cua user khac neu can ho tro refund.

## 13. Cac diem can can than khi sua code

- Moi thay doi payment/refund nen biendich va smoke test `/staff/dashboard`, `/staff/settlement`, `/my-contracts`.
- Cac method co `conn.setAutoCommit(false)` phai rollback trong catch va commit khi thanh cong.
- Khi xu ly webhook phai giu idempotent:
  - Duplicate webhook khong duoc thu tien hai lan.
  - Transaction da PAID thi tra duplicate/success hop ly.
- Khong sua truc tiep status string neu chua kiem tra JSP va SQL constraints.
- Neu them payment type/status moi, can sua:
  - Model enum/constants.
  - SQL migration.
  - JSP display.
  - Mapper/DAO query.
- Khi sua PayOS config, khong commit key that vao repo.

## 14. Huong dan doc code theo thu tu

Neu muon hoc/bao ve project, nen doc theo thu tu:

1. `README.md`: cach chay.
2. `database-schema.sql`: bang va quan he.
3. `DBContext.java`: ket noi DB.
4. `AuthFilter.java`, `CharacterEncodingFilter.java`: request filtering.
5. `LoginServlet`, `RegisterServlet`, `ProfileServlet`: user basic.
6. `CarSearchServlet`, `CarCatalogServlet`, `BookingServlet`: luong dat xe.
7. `CheckoutService`, `ContractDAO`: tao hop dong.
8. `PaymentDAO`, cac `Payment*Workflow`, `Payment*Store`: thanh toan/quyet toan.
9. `SettlementServlet`: staff quyet toan.
10. `SupportServlet`, `StaffSupportServlet`: support ticket.
11. JSP tuong ung voi tung servlet.

## 15. Checklist khi them tinh nang moi

- Them/cap nhat model neu can field moi.
- Them migration SQL.
- Them DAO method hoac workflow method.
- Them servlet action.
- Them JSP/JS display.
- Them validation server-side.
- Chay compile.
- Test route chinh.
- Cap nhat README/docs neu tinh nang thay doi luong chay.
