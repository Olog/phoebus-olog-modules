/**
 * Copyright (C) 2021 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.olog.kafka;

import org.phoebus.olog.entity.Log;
import org.phoebus.olog.notification.LogEntryNotifier;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaNotifier implements LogEntryNotifier {

    private KafkaProducer kafkaProducer;
    private String ologRootURL;

    private Logger logger = Logger.getLogger(KafkaNotifier.class.getName());

    public KafkaNotifier(){
        configure();
    }

    public void configure(){
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/kafka_integration_config.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to load configuration.", e);
            return;
        }
        ologRootURL = System.getProperty("olog.web.rootURL",
                properties.getProperty("olog.web.rootURL"));
        logger.log(Level.INFO, "Using Olog web root URL: " + ologRootURL);
        kafkaProducer = KafkaProducer.getInstance();
    }

    @Override
    public void notify(Log log) {
        LogEntryMessage logEntryMessage = LogEntryMessage.fromLog(ologRootURL, log);
        kafkaProducer.send(logEntryMessage);
    }
}
