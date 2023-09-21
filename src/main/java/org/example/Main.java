package org.example;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.plaf.ButtonUI;
import java.io.*;
import java.sql.*;
import java.util.Calendar;

public class Main {
    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "root";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                                                        //Где INSENSITIVE - без изменений в бд
                                                        //    CONCUR_READ_ONLY - режим чтения

            ResultSet resultSet = statement.executeQuery("select * from dish"); // Здесь stat получает scrollable resultSet.
            if (resultSet.next()){ // Перейти к следующдей записи
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.next()){
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.previous()){ // Перейти к предыдущей записи
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.relative(2)){ // Перейти вперед на 2 шага
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.relative(-2)){ // Перейти назад на 2 шага
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.absolute(2)){ // Перейти на 2-ую запись
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.first()){ // Перейти на 1-ую запись
                System.out.println(resultSet.getString("title"));
            }
            if (resultSet.last()){// Перейти на последнюю запись
                System.out.println(resultSet.getString("title"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main (String[]args){
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);Statement stat = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            BufferedImage image = ImageIO.read(new File("logo.png"));
//            Blob blob = conn.createBlob();
//            try (OutputStream outputStream = blob.setBinaryStream(1)){
//                ImageIO.write(image, "png", outputStream);
//            }
//
//            // Добавление картинки в БД.
////            PreparedStatement statement = conn.prepareStatement("insert into dish (id, icon) value (?,?)");
////            statement.setInt(1,23);
////            statement.setBlob(2,blob);
////            statement.execute();
//
//            ResultSet resultSet = stat.executeQuery("select * from dish");
//            while (resultSet.next()){
//                Blob blob2 = resultSet.getBlob("icon");
//                BufferedImage image2 = ImageIO.read(blob2.getBinaryStream());
//                File outputFile = new File("saved.png");
//                ImageIO.write(image2,"png", outputFile);
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Нет коннекта!");
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    private static final String INSERT_NEW = "INSERT INTO dish VALUES(?,?,?,?,?,?,?)";
//    private static final String GET_ALL = "SELECT * FROM dish";
//
//    private static final String DELETE = "DELETE FROM dish WHERE id=?";
//
//    public static void main (String[]args){
//
//        PreparedStatement preparedStatement = null;
//        //try с ресурсами в скобках которые не надо закрывать вручную.
//        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
//            if (!connection.isClosed()) System.out.println("We are connected!");
//
////            preparedStatement = connection.prepareStatement(INSERT_NEW);
////            preparedStatement.setInt(1,2);
////            preparedStatement.setString(2,"Inserted title");
////            preparedStatement.setString(3, "Inserted desc");
////            preparedStatement.setFloat(4, 0.2f);
////            preparedStatement.setBoolean(5,true);
////            preparedStatement.setDate(6,new Date(Calendar.getInstance().getTimeInMillis()));
////            preparedStatement.setBlob(7, new FileInputStream("logo.png"));
////
////            preparedStatement.execute();
//
//            preparedStatement = connection.prepareStatement(DELETE);
//
//            preparedStatement.setInt(1,2);
//            preparedStatement.executeUpdate();
//
//            preparedStatement = connection.prepareStatement(GET_ALL);
//
//            ResultSet res = preparedStatement.executeQuery();
//
//            while (res.next()){
//                int id = res.getInt("id");
//                String title = res.getString("title");
//                String desc = res.getString("description");
//                float rating = res.getFloat("rating");
//                boolean published = res.getBoolean("published");
//                Date date = res.getDate("created");
//                byte[] icon = res.getBytes("icon");
//
//                System.out.println("id: "+id+", title: "+title+", description: "+desc+", rating: "+rating+"" +
//                        ", published: "+published+", date: "+date+", icon lenght: "+icon.length);
//
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
////        } catch (FileNotFoundException e) {
////            throw new RuntimeException(e);
//        } finally {
//            try {
//                preparedStatement.close();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}


//Урок 6 "ResultSet получение данных"

//public class Main {
//    public static void main(String[] args) {
//        DBWorker worker = new DBWorker();
//
//        String query = "select * from users";
//
//        try {
//            Statement statement = worker.getConnection().createStatement();
//            ResultSet resultSet = statement.executeQuery(query);
//
//            while (resultSet.next()){
//                User user = new User();
////                user.setId(resultSet.getInt(1));                              // Используя порядковые номера колонок начиная с 1
////                user.setUsername(resultSet.getString(2));
////                user.setPassword(resultSet.getString(3));
//
//                user.setId(resultSet.getInt("id"));                  // Используя названия колонок
//                user.setUsername(resultSet.getString("username"));
//                user.setPassword(resultSet.getString("password"));
//
//                System.out.println(user);
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}


//Урок 5 "Статические запросы"

//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        //try с ресурсами в скобках которые не надо закрывать вручную.
//        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement = connection.createStatement()) {
//            if (!connection.isClosed()) System.out.println("We are connected!");
//
//            //statement.execute("INSERT INTO users(name,age,email) VALUES ('Artem',24,'artem@mail.ru');");    // Сделать запись в бд
//            //int res = statement.executeUpdate("update users set name = 'New Name' where id = 5;");          // Обновить запись в бд
//            //ResultSet res = statement.executeQuery("select * from users;");                                 // Получить записи из бд*
//
//            //System.out.println(res);
//
////            statement.addBatch("insert into users (name, age, email) values('Name2',25,'name2@mail.ru');"); // Составить несколько запросов
////            statement.addBatch("insert into users (name, age, email) values('Name2',25,'name2@mail.ru');");
////            statement.addBatch("insert into users (name, age, email) values('Name3',25,'name3@mail.ru');");
////
////            statement.executeBatch();                                                                       // Выполнить нескольно запросов
////
////            statement.clearBatch();                                                                         // Очистить запросы
//
//            System.out.println(statement.getConnection());
//        } catch (SQLException e) {
//            System.out.println("there is no connection... Exception!");
//        }
//    }
//}