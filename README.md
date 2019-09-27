# BackgroundRemoval
Background là ứng dụng cho phép chụp ảnh sau đó giữ lấy những đối tượng là người và xóa phông nền
Ứng dụng sử Machine Learning model để dự đoán và xóa phông nền

## Library

Trong ứng dụng có package là library
Có 4 component chính:
-VisionPredictor: dùng để quản lý và thực thi model
-VisionImage: xử lý ảnh chụp được để chuẩn bị cho việc dự đoán của model
-OnDeviceModel: chứa thông tin và load model file cho việc dự đoán
-VisionResult: Truy cập vào kết quả của việc dự đoán

Class VisionPredictor xử lý tất cả các xử lý phức tạp đi kèm với việc chạy model trên thiết bị, trong đó có việc tiền xử lý (pre-processing - đây là các thao tác cần thiết để chuẩn bị ảnh để định dạng input cho model) và hậu xử lý (post-processing - xử lý đầu ra từ model để lấy kết quả mong muốn)
Với mỗi predictor chứa phương thức predict lấy VisionImage làm đối số và trả về là VisionResult

