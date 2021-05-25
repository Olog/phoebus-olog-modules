/*
 * Copyright (C) 2020 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.olog.kafka;

import org.phoebus.olog.entity.Log;
import org.phoebus.olog.entity.Logbook;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom message pojo intended for Kafka messaging.
 */
public class LogEntryMessage {

    private String url;
    private List<String> logbooks;
    private String title;
    private String author;
    private String body;

    /**
     * Transforms a {@link Log} object to something customized for Kafka messaging, i.e. a {@link LogEntryMessage} object.
     * @param ologRootURL The root URL used to construct a full URL to the log record. Typical use case is
     *                    a web client able to render the log entry from the full URL.
     * @param log The log entry as persisted in Elastic/MongoDB
     * @return A {@link LogEntryMessage}
     */
    public static LogEntryMessage fromLog(String ologRootURL, Log log){
        LogEntryMessage logEntryMessage = new LogEntryMessage();
        logEntryMessage.setAuthor(log.getOwner());
        logEntryMessage.setBody(log.getSource());
        logEntryMessage.setTitle(log.getTitle());
        logEntryMessage.setUrl(ologRootURL + "/" + log.getId().toString());
        logEntryMessage.setLogbooks(log.getLogbooks().stream().map(Logbook::getName).collect(Collectors.toList()));
        return logEntryMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getLogbooks() {
        return logbooks;
    }

    public void setLogbooks(List<String> logbooks) {
        this.logbooks = logbooks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
