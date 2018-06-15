package com.weather;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionDAO {
    private final Map<Long, String> subscriptions;
    private JdbcTemplate jtm;

    public SubscriptionDAO(JdbcTemplate jtm) {
        this.jtm = jtm;
        subscriptions = new ConcurrentHashMap<>();
    }

    public List<Subscription> getAll() {
        return jtm.query("select * from subscriptions", new RowMapper<Subscription>() {
            @Override
            public Subscription mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Subscription(resultSet.getLong(1), resultSet.getString(2));
            }
        });

//        return subscriptions.entrySet().stream()
//                .map((Map.Entry<Long, String> e) -> {
//                    return new Subscription(e.getKey(), e.getValue());
//                })
//                .collect(Collectors.toList());
    }

    public void remove(Long chatId) {
        jtm.execute("delete from subscriptions where chatid = " + chatId);
    }

    public Subscription check(Long chatId) {
        try {
            return (Subscription) jtm.queryForObject("select * from subscriptions where chatid = " + chatId, new BeanPropertyRowMapper(Subscription.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void update(Long chatId, String topic) {
        jtm.execute("update subscriptions set topic = '" + topic + "' where chatid = " + chatId);
    }

    public void put(Long chatId, String topic) {
        Subscription sub = check(chatId);
        if (sub != null) {
            update(chatId, topic);
        } else {
            jtm.execute("insert into subscriptions (chatid, topic) values (" + chatId + ",'" + topic + "')");
        }
    }
}
