/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.mobile.client.apps.news.events.pages;

import org.silverpeas.mobile.shared.dto.navigation.ApplicationInstanceDTO;
import org.silverpeas.mobile.shared.dto.news.NewsDTO;

import javax.ws.rs.core.Application;
import java.util.List;

public class NewsLoadedEvent extends AbstractNewsPagesEvent {

  private List<NewsDTO> news;
  private ApplicationInstanceDTO instance;

  public NewsLoadedEvent(ApplicationInstanceDTO instance, List<NewsDTO> news){
    super();
    this.news = news;
    this.instance = instance;
  }

  @Override
  protected void dispatch(NewsPagesEventHandler handler) {
    handler.onNewsLoad(this);
  }

  public List<NewsDTO> getNews() {
    return news;
  }

  public ApplicationInstanceDTO getApplicationInstance() {
    return instance;
  }
}
