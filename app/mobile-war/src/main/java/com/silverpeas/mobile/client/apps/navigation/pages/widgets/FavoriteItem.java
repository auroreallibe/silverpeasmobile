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

package com.silverpeas.mobile.client.apps.navigation.pages.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.silverpeas.mobile.client.SpMobil;
import com.silverpeas.mobile.client.apps.navigation.events.pages.ClickItemEvent;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.ShortCutRouter;
import com.silverpeas.mobile.client.resources.ApplicationMessages;
import com.silverpeas.mobile.shared.dto.ContentsTypes;
import com.silverpeas.mobile.shared.dto.FavoriteDTO;

public class FavoriteItem extends Composite {

  private FavoriteDTO data;
  private static FavoriteItemUiBinder uiBinder = GWT.create(FavoriteItemUiBinder.class);
  @UiField Anchor link;
  protected ApplicationMessages msg = null;


  interface FavoriteItemUiBinder extends UiBinder<Widget, FavoriteItem> {
  }

  public FavoriteItem() {
    initWidget(uiBinder.createAndBindUi(this));
    msg = GWT.create(ApplicationMessages.class);
  }

  public void setData(FavoriteDTO data) {
    this.data = data;
    link.setText(data.getName());

    if(data.getUrl().startsWith("/")) {
      // internal link
      link.setHref("#");
    } else {
      link.setHref(data.getUrl());
      link.setTarget("_blank");
    }
    link.setStyleName("ui-btn ui-icon-carat-r");
  }

  @UiHandler("link")
  protected void onClick(ClickEvent event) {
    if(data.getUrl().startsWith("/")) {
      String shortcutContentType = "";
      String shortcutContentId = data.getUrl().substring(data.getUrl().lastIndexOf("/") + 1);
      if (data.getUrl().contains("Publication")) {
        shortcutContentType = ContentsTypes.Publication.name();
      } else if (data.getUrl().contains("Media")) {
        shortcutContentType = ContentsTypes.Media.name();
      }
      ShortCutRouter.route(SpMobil.user, null, shortcutContentType, shortcutContentId);
    }
  }

}
