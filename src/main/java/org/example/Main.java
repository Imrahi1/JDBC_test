package org.example;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import javax.swing.plaf.ButtonUI;
import java.io.*;
import java.sql.*;
import java.util.Calendar;


public class Main {
    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "root";

    public static void main(String[] args) throws SQLException, InterruptedException {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stat = conn.createStatement()) {
            if (!conn.isClosed()) System.out.println("We are connected!");

            conn.setAutoCommit(false); // Включить ожидание commit

            // Phantom Reads - добаление данных вне основной транзакции.
//            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Установка уровня изоляции, который не защищает от PhantomReads
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); // Установка уровня изоляции, который защищает от PhantomReads

            ResultSet resultSet = stat.executeQuery("select * from dish"); // Выполняем запрос и вывод данных, который выводит нам не измененные данные
            while (resultSet.next()) {
                System.out.println(resultSet.getInt("id"));
                System.out.println(resultSet.getString("title"));
            }

            new OtherTransaction().start(); // Добавляем данные во втором (коннешнене) потоке
            Thread.sleep(2000);

            ResultSet resultSet2 = stat.executeQuery("select * from dish"); // Выполняем запрос и вывод данных, который выводит нам измененные данные
            while (resultSet2.next()) {
                System.out.println(resultSet2.getInt("id"));
                System.out.println(resultSet2.getString("title"));
            }
        }
    }

    static class OtherTransaction extends Thread {
        @Override
        public void run() {
            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                 Statement stat = conn.createStatement()) {
                if (!conn.isClosed()) System.out.println("We are connected2!");

                conn.setAutoCommit(false); // Включить ожидание commit
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                stat.executeUpdate("insert into dish (title) values ('new value2')");
                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) throws SQLException, InterruptedException {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement stat = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            conn.setAutoCommit(false); // Включить ожидание commit
//
//            // Non-Repeatable Reads - изменение данных вне основной транзакции.
//            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); // Установка уровня изоляции, который защищает от REPEATABLE_READ
////            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Установка уровня изоляции, который не защищает от REPEATABLE_READ
//
//            ResultSet resultSet = stat.executeQuery("select * from dish"); // Выполняем запрос и вывод данных, который выводит нам не измененные данные
//            while (resultSet.next()) {
//                System.out.println(resultSet.getInt("id"));
//                System.out.println(resultSet.getString("title"));
//            }
//
//            new OtherTransaction().start(); // Меняем данные во втором (коннешнене) потоке
//            Thread.sleep(2000);
//
//            ResultSet resultSet2 = stat.executeQuery("select * from dish"); // Выполняем запрос и вывод данных, который выводит нам измененные данные
//            while (resultSet2.next()) {
//                System.out.println(resultSet2.getInt("id"));
//                System.out.println(resultSet2.getString("title"));
//            }
//        }
//    }
//
//    static class OtherTransaction extends Thread {
//        @Override
//        public void run() {
//            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//                 Statement stat = conn.createStatement()) {
//                if (!conn.isClosed()) System.out.println("We are connected2!");
//
//                conn.setAutoCommit(false); // Включить ожидание commit
//                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//                stat.executeUpdate("update dish set title = 'new value' where id = 23");
//                conn.commit();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}

// DirtyRead
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) throws SQLException, InterruptedException {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement stat = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            conn.setAutoCommit(false); // Включить ожидание commit
//
//            // Dirty Read - чтение несуществующей инфы.
//            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); // Установка уровня изоляции, который не защищает от DirtyRead
//
//            stat.execute("update dish set title = 'new value' where id = 23"); // Меняем данные
//            new OtherTransaction().start(); // Выполняем запрос и вывод данных, который выводит нам измененные данные
//            Thread.sleep(2000);
//            conn.rollback(); // Откатываем конекшн тем самым откатывая обновления на 28 строке
//
//            // В итоге получается что мы вывели данные в бд, которых в бд на самом деле нет.
//            // Решение: уровень TRANSACTION_READ_COMMITTED
//
//        }
//    }
//
//
//    static class OtherTransaction extends Thread {
//        @Override
//        public void run() {
//            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//                 Statement stat = conn.createStatement()) {
//                if (!conn.isClosed()) System.out.println("We are connected2!");
//
//                conn.setAutoCommit(false); // Включить ожидание commit
//                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
//
//                ResultSet resultSet = stat.executeQuery("select * from dish");
//                while (resultSet.next()) {
//                    System.out.println(resultSet.getInt("id"));
//                    System.out.println(resultSet.getString("title"));
//                }
//                conn.rollback();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}



//Транзакции
//public class Main {
//
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement stat = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            conn.setAutoCommit(false); // Включить ожидание commit
//
//            stat.executeUpdate("insert into dish (title) values ('transInfo1')");
//            Savepoint savepoint = conn.setSavepoint(); // Установка сейва для rollback, т.е. все что ниже откатывается.
//            stat.executeUpdate("insert into dish (title) values ('transInfo2')");
//            stat.executeUpdate("insert into dish (title) values ('transInfo3')");
//
//
//            conn.rollback(savepoint); // сделать откат, можно к сейвпоинту если его указать в аргументах.
//            conn.commit(); // сделать commit
//
//            //conn.releaseSavepoint(savepoint); // не понятно)
//
//            // Зачастую rollback используют в catch, а внизу тела try ставят commit.
//            // Таким образом код закоммитится только если не было ошибок, если же ошибки были, то изменения rollback-ются.
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}



//Мета-данные
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement statement = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            // Взять метаданные из бд
//            DatabaseMetaData databaseMetaData = conn.getMetaData();
//            ResultSet tables = databaseMetaData.getTables(null, null, null, new String[]{"Table"});
//            while (tables.next()){
//                System.out.println(tables.getString(3));
//            }
//            System.out.println("==================================");
//
//            // Взять метаданные из выборки
//            ResultSet resultSet = statement.executeQuery("select * from dish");
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            for (int i = 1; i <= metaData.getColumnCount(); i++) {
//                System.out.println(metaData.getColumnLabel(i));
//                System.out.println(metaData.getColumnType(i));
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) throws SQLException {
//        ResultSet resultSet = getData();
//        while (resultSet.next()){
//            System.out.println(resultSet.getInt("id"));
//            System.out.println(resultSet.getString("title"));
//        }
//
////        CachedRowSet cachedRowSet = (CachedRowSet) resultSet; // Превращаем resultSet в CachedRowSet
//
////        Получить данные
////        cachedRowSet.setUrl(URL);
////        cachedRowSet.setUsername(USERNAME);
////        cachedRowSet.setPassword(PASSWORD);
//
////        cachedRowSet.setCommand("select * from dish where id = ?"); // Передать запрос к выборке
////        cachedRowSet.setInt(1,23); // передать в переменную ? под номером 1 значение.
////        cachedRowSet.setPageSize(20); // если записей много, могу их ограничить
////        cachedRowSet.execute();
////        do {
////            while (cachedRowSet.next()){
////                System.out.println(cachedRowSet.getInt("id"));
////                System.out.println(cachedRowSet.getString("title"));
////            }
////        } while (cachedRowSet.nextPage());
//
////      Изменить данные
//        CachedRowSet cachedRowSet2 = (CachedRowSet) resultSet;
//        cachedRowSet2.setTableName("dish");
//        cachedRowSet2.absolute(1);
//        cachedRowSet2.deleteRow();
//        cachedRowSet2.beforeFirst();
//        while (cachedRowSet2.next()){
//            System.out.println(cachedRowSet2.getInt("id"));
//            System.out.println(cachedRowSet2.getString("title"));
//        }
//
//        //Применение изменений
//        //1 способ
////        cachedRowSet2.acceptChanges(DriverManager.getConnection(URL,USERNAME,PASSWORD));
//
//        //2 способ
//        cachedRowSet2.setUrl(URL);
//        cachedRowSet2.setUsername(USERNAME);
//        cachedRowSet2.setPassword(PASSWORD);
//        cachedRowSet2.acceptChanges(); // Не работает :(
//    }
//
//
//    private static ResultSet getData(){
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
//
//            RowSetFactory factory = RowSetProvider.newFactory(); // создатель RowSet-ов
//            CachedRowSet cachedRowSet = factory.createCachedRowSet(); // создаем кешированный RowSet, хотя есть и другие (гугл)
//
//
//            ResultSet resultSet = statement.executeQuery("select * from dish");
//            cachedRowSet.populate(resultSet); // Передаем в кешированный RowSet обычный. (Кешируем RowSet)
//            return cachedRowSet;
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}








//Бежим по ResultSet и обновляем
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
//                                                        //Где SENSITIVE - c изменениями в бд
//                                                        //    CONCUR_UPDATABLE - режим записи
////             PreparedStatement preparedStatement = conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
//        ) {
//            ResultSet resultSet = statement.executeQuery("select * from dish");
//            while (resultSet.next()){
//                System.out.println(resultSet.getInt("id"));
//                System.out.println(resultSet.getString("title"));
//            }
//            resultSet.last();
//            resultSet.updateString("title", "new Value");
//            resultSet.updateRow();
//
//            resultSet.moveToInsertRow(); // Перейти к созданию нового поля
//            resultSet.updateString("title", "inserted row");
//            resultSet.insertRow();
//
//            resultSet.absolute(2);
//            resultSet.deleteRow();
//
//            resultSet.beforeFirst(); // Перейти выше первого поля (нужно для валидного вывода с циклом .next())
//            while (resultSet.next()){
//                System.out.println(resultSet.getInt("id"));
//                System.out.println(resultSet.getString("title"));
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

//Бежим по ResultSet и читаем
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                                                        //Где INSENSITIVE - без изменений в бд
//                                                        //    CONCUR_READ_ONLY - режим чтения
////             PreparedStatement preparedStatement = conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
//        ) {
//
//            ResultSet resultSet = statement.executeQuery("select * from dish"); // Здесь stat получает scrollable resultSet.
//            if (resultSet.next()){ // Перейти к следующдей записи
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.next()){
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.previous()){ // Перейти к предыдущей записи
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.relative(2)){ // Перейти вперед на 2 шага
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.relative(-2)){ // Перейти назад на 2 шага
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.absolute(2)){ // Перейти на 2-ую запись
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.first()){ // Перейти на 1-ую запись
//                System.out.println(resultSet.getString("title"));
//            }
//            if (resultSet.last()){// Перейти на последнюю запись
//                System.out.println(resultSet.getString("title"));
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

//Загрузка картинки в код и в БД
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    public static void main (String[]args){
//        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement stat = conn.createStatement()) {
//            if (!conn.isClosed()) System.out.println("We are connected!");
//
//            BufferedImage image = ImageIO.read(new File("logo.png")); // Закидываем картинку в переменную
//            Blob blob = conn.createBlob(); // Создаем пустую переменную типа Blob
//            try (OutputStream outputStream = blob.setBinaryStream(1)){ // Создаем поток который будет грузить картинку в Blob переменную
//                ImageIO.write(image, "png", outputStream); // Активируем загрузку
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

//Установка данных
//public class Main {
//    private final static String URL = "jdbc:mysql://localhost:3306/mydbtest";
//    private final static String USERNAME = "root";
//    private final static String PASSWORD = "root";
//
//    private static final String INSERT_NEW = "INSERT INTO dish VALUES(?,?,?,?,?,?,?)"; // ? - переменные по индексам
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
////            preparedStatement.setInt(1,2); // установка значений в переменные из INSERT_NEW, где 1-ый агр это индекс переменной
////            preparedStatement.setString(2,"Inserted title");
////            preparedStatement.setString(3, "Inserted desc");
////            preparedStatement.setFloat(4, 0.2f);
////            preparedStatement.setBoolean(5,true);
////            preparedStatement.setDate(6,new Date(Calendar.getInstance().getTimeInMillis()));
////            preparedStatement.setBlob(7, new FileInputStream("logo.png"));
////            preparedStatement.execute(); // Применить установку данных
//
//            preparedStatement = connection.prepareStatement(DELETE);
//            preparedStatement.setInt(1,2); // Установить в переменную из DELETE значение
//            preparedStatement.executeUpdate(); // Применить обновление данных
//
//            preparedStatement = connection.prepareStatement(GET_ALL);
//            ResultSet res = preparedStatement.executeQuery(); // Применить запрос данных
//
//            Вывод данных
//            while (res.next()){ // бежим по строкам бд
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
//            ResultSet resultSet = statement.executeQuery(query); // Применяем запрос данных
//
//            while (resultSet.next()){ // бежим по строкам бд
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
//        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//             Statement statement = connection.createStatement()) {
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
////            statement.executeBatch();                                                                       // Выполнить нескольно запросов, возвращает int массив с инфой о запросах.
////
////            statement.clearBatch();                                                                         // Очистить запросы
//
//            System.out.println(statement.getConnection()); //Просто по приколу
//        } catch (SQLException e) {
//            System.out.println("there is no connection... Exception!");
//        }
//    }
//}