/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 */

package org.silverpeas.mobile.client.apps.formsonline.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.silverpeas.mobile.client.apps.blog.events.app.BlogLoadEvent;
import org.silverpeas.mobile.client.apps.blog.events.pages.AbstractBlogPagesEvent;
import org.silverpeas.mobile.client.apps.blog.resources.BlogMessages;
import org.silverpeas.mobile.client.apps.favorites.pages.widgets.AddToFavoritesButton;
import org.silverpeas.mobile.client.apps.formsonline.events.pages.AbstractFormsOnlinePagesEvent;
import org.silverpeas.mobile.client.apps.formsonline.events.pages.FormsOnlineLoadedEvent;
import org.silverpeas.mobile.client.apps.formsonline.events.pages.FormsOnlinePagesEventHandler;
import org.silverpeas.mobile.client.apps.formsonline.resources.FormsOnlineMessages;
import org.silverpeas.mobile.client.common.EventBus;
import org.silverpeas.mobile.client.components.UnorderedList;
import org.silverpeas.mobile.client.components.base.ActionsMenu;
import org.silverpeas.mobile.client.components.base.PageContent;

public class FormsOnlinePage extends PageContent implements FormsOnlinePagesEventHandler {

  private static FormsOnlinePageUiBinder uiBinder = GWT.create(FormsOnlinePageUiBinder.class);

  @UiField(provided = true) protected FormsOnlineMessages msg = null;
  @UiField
  ActionsMenu actionsMenu;

  private AddToFavoritesButton favorite = new AddToFavoritesButton();
  private String instanceId;

  @Override
  public void onFormsOnlineLoad(final FormsOnlineLoadedEvent event) {

  }

  interface FormsOnlinePageUiBinder extends UiBinder<Widget, FormsOnlinePage> {
  }

  public FormsOnlinePage() {
    msg = GWT.create(FormsOnlineMessages.class);
    setPageTitle(msg.title());
    initWidget(uiBinder.createAndBindUi(this));
    EventBus.getInstance().addHandler(AbstractFormsOnlinePagesEvent.TYPE, this);
  }

  @Override
  public void stop() {
    super.stop();
    EventBus.getInstance().removeHandler(AbstractFormsOnlinePagesEvent.TYPE, this);
  }


}