package com.weather;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        SimpleDriverDataSource dataSource = getSource();
        JdbcTemplate jtm = new JdbcTemplate(dataSource);
        initDb(jtm);

//        String sql = "SELECT * FROM SUBSCRIPTIONS WHERE Chatid=?";
//        Long chatid = 134538L;
//
//        Subscription subscription = (Subscription) jtm.queryForObject(sql, new Object[]{chatid}, new BeanPropertyRowMapper(Subscription.class));


        ApiContextInitializer.init();

        SubscriptionDAO dao = new SubscriptionDAO(jtm);

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            TinyHedgehodBot bot = new TinyHedgehodBot(dao);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private static SimpleDriverDataSource getSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        dataSource.setUrl("jdbc:derby:tgbotdb;user=DEMO;create=true");
        return dataSource;
    }

    private static void initDb(JdbcTemplate jtm) throws SQLException {
        try (Connection connection = jtm.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, "DEMO", "SUBSCRIPTIONS", null);

            if (!rs.next()) {
                createTables(jtm);
            }
        }
    }

    private static void createTables(JdbcTemplate jtm) {
        jtm.execute("CREATE TABLE SUBSCRIPTIONS (CHATID BIGINT NOT NULL PRIMARY KEY, TOPIC VARCHAR(30) NOT NULL)");
    }

}
