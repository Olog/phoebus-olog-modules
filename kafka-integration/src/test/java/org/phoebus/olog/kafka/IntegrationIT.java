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

import org.junit.Test;
import org.phoebus.olog.entity.Log;
import org.phoebus.olog.entity.Log.LogBuilder;
import org.phoebus.olog.notification.LogEntryNotifier;

import java.util.ServiceLoader;

public class IntegrationIT {

    /**
     * Just a simple test to verify that producer is set up properly.
     */
    @Test
    public void testProducer(){
        Log log = LogBuilder.createLog().build();
        log.setId(777L);
        ServiceLoader<LogEntryNotifier> loader = ServiceLoader.load(LogEntryNotifier.class);
        loader.stream().forEach(p -> {
            LogEntryNotifier notifier = p.get();
            notifier.notify(log);
        });
    }
}
