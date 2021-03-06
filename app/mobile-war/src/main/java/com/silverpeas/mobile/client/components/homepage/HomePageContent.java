/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.silverpeas.mobile.client.components.homepage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.silverpeas.mobile.client.SpMobil;
import com.silverpeas.mobile.client.apps.config.events.app.AbstractConfigAppEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.external
    .NavigationAppInstanceChangedEvent;
import com.silverpeas.mobile.client.apps.navigation.events.pages.AbstractNavigationPagesEvent;
import com.silverpeas.mobile.client.apps.navigation.events.pages.ClickItemEvent;
import com.silverpeas.mobile.client.apps.navigation.events.pages.HomePageLoadedEvent;
import com.silverpeas.mobile.client.apps.navigation.events.pages.NavigationPagesEventHandler;
import com.silverpeas.mobile.client.apps.navigation.pages.NavigationPage;
import com.silverpeas.mobile.client.apps.navigation.pages.widgets.FavoriteItem;
import com.silverpeas.mobile.client.apps.navigation.pages.widgets.NavigationItem;
import com.silverpeas.mobile.client.apps.navigation.pages.widgets.NewsItem;
import com.silverpeas.mobile.client.apps.navigation.pages.widgets.PublicationItem;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.Notification;
import com.silverpeas.mobile.client.common.gwt.SuperDevModeUtil;
import com.silverpeas.mobile.client.common.mobil.MobilUtils;
import com.silverpeas.mobile.client.common.reconizer.swipe.SwipeEndEvent;
import com.silverpeas.mobile.client.common.reconizer.swipe.SwipeEndHandler;
import com.silverpeas.mobile.client.common.reconizer.swipe.SwipeEvent;
import com.silverpeas.mobile.client.common.reconizer.swipe.SwipeRecognizer;
import com.silverpeas.mobile.client.components.UnorderedList;
import com.silverpeas.mobile.client.resources.ApplicationMessages;
import com.silverpeas.mobile.shared.dto.FavoriteDTO;
import com.silverpeas.mobile.shared.dto.HomePageDTO;
import com.silverpeas.mobile.shared.dto.configuration.Config;
import com.silverpeas.mobile.shared.dto.documents.PublicationDTO;
import com.silverpeas.mobile.shared.dto.navigation.ApplicationInstanceDTO;
import com.silverpeas.mobile.shared.dto.navigation.SilverpeasObjectDTO;
import com.silverpeas.mobile.shared.dto.navigation.SpaceDTO;
import com.silverpeas.mobile.shared.dto.news.NewsDTO;

import java.util.List;

public class HomePageContent extends Composite implements SwipeEndHandler {

  private SwipeRecognizer swipeRecognizer;
  private static HomePageUiBinder uiBinder = GWT.create(HomePageUiBinder.class);
  private HomePageDTO data;
  private int currentNewsIndex = 0;
  private Config config = null;

  @UiField(provided = true)
  protected ApplicationMessages msg = null;

  @UiField
  UnorderedList favoris, lastPublications, spaces, news;
  @UiField
  HTMLPanel lastPublicationsSection, favorisSection;

  interface HomePageUiBinder extends UiBinder<Widget, HomePageContent> {}

  public HomePageContent() {
    msg = GWT.create(ApplicationMessages.class);
    initWidget(uiBinder.createAndBindUi(this));
    Config conf = SpMobil.getConfiguration();
    setConfig(conf);
    EventBus.getInstance().addHandler(SwipeEndEvent.getType(), this);
  }

  public void setConfig(final Config config) {
    this.config = config;
    favorisSection.setVisible(config.isFavoritesDisplay());
    lastPublicationsSection.setVisible(config.isLastPublicationsDisplay());
    news.setVisible(config.isNewsDisplay());
  }

  public void setData(HomePageDTO data) {
    this.data = data;

    news.clear();
    if (config.isNewsDisplay()) {
      List<NewsDTO> newsDTOList = data.getNews();
      int i = 1;
      boolean v = true;
      int max = newsDTOList.size();
      for (NewsDTO newsDTO : newsDTOList) {
        NewsItem item = new NewsItem();
        item.setData(i, max, newsDTO);
        item.setVisible(v);
        news.add(item);
        i++;
        v = false;
      }
    }

    spaces.clear();
    List<SilverpeasObjectDTO> spacesList = data.getSpacesAndApps();
    for (SilverpeasObjectDTO space : spacesList) {
      NavigationItem item = new NavigationItem();
      item.setData(space);
      spaces.add(item);
    }


    favoris.clear();
    List<FavoriteDTO> favoritesList = data.getFavorites();
    favorisSection.setVisible(!favoritesList.isEmpty() && config.isFavoritesDisplay());
    for (FavoriteDTO favoriteDTO : favoritesList) {
      FavoriteItem item = new FavoriteItem();
      item.setData(favoriteDTO);
      favoris.add(item);
    }


    lastPublications.clear();
    List<PublicationDTO> publicationsList = data.getLastPublications();
    lastPublicationsSection.setVisible(!publicationsList.isEmpty() && config.isLastPublicationsDisplay());
    for (PublicationDTO publicationDTO : publicationsList) {
      PublicationItem item = new PublicationItem();
      item.setData(publicationDTO);
      lastPublications.add(item);
    }


    if (MobilUtils.isMobil()) {
      Element e = Document.get().getElementById("actus");
      HTML actus = HTML.wrap(e);
      swipeRecognizer = new SwipeRecognizer(actus);
    }

    Notification.activityStop();
  }

  @Override
  public void onSwipeEnd(final SwipeEndEvent event) {
    if (isVisible()) {
      if (event.getDirection() == SwipeEvent.DIRECTION.RIGHT_TO_LEFT) {
        // next
        if (currentNewsIndex == news.getWidgetCount() - 1) {
          currentNewsIndex = 0;
        } else {
          currentNewsIndex++;
        }
        updateNewsView();
      } else if (event.getDirection() == SwipeEvent.DIRECTION.LEFT_TO_RIGHT) {
        // previous
        if (currentNewsIndex == 0) {
          currentNewsIndex = news.getWidgetCount() - 1;
        } else {
          currentNewsIndex--;
        }
        updateNewsView();
      }
    }
  }

  public void stop() {
    EventBus.getInstance().removeHandler(SwipeEndEvent.getType(), this);
  }

  private void updateNewsView() {
    for (int i = 0; i < news.getWidgetCount(); i++) {
      ((NewsItem) news.getWidget(i)).setVisible(i == currentNewsIndex);
    }
  }
}
