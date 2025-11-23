package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserPopulator {

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private DatabaseConduit databaseConduit;

    public void populate() {
        String[] userLines = fileLoader.loadStrings("/test_data/lkjhgfdsa.hjkl");
        for (String userLine : userLines) {
            String[] userData = userLine.split(", ");
            User user = new User(userData[0], new BigDecimal(userData[1]));
            databaseConduit.save(user);
        }
    }
}
