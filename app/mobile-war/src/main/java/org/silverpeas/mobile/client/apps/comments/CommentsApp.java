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

package org.silverpeas.mobile.client.apps.comments;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import org.silverpeas.mobile.client.apps.comments.events.app.AbstractCommentsAppEvent;
import org.silverpeas.mobile.client.apps.comments.events.app.AddCommentEvent;
import org.silverpeas.mobile.client.apps.comments.events.app.CommentsAppEventHandler;
import org.silverpeas.mobile.client.apps.comments.events.app.CommentsLoadEvent;
import org.silverpeas.mobile.client.apps.comments.events.pages.CommentAddedEvent;
import org.silverpeas.mobile.client.apps.comments.events.pages.CommentsLoadedEvent;
import org.silverpeas.mobile.client.apps.comments.pages.CommentsPage;
import org.silverpeas.mobile.client.common.EventBus;
import org.silverpeas.mobile.client.common.ServicesLocator;
import org.silverpeas.mobile.client.common.app.App;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOnly;
import org.silverpeas.mobile.client.common.network.AsyncCallbackOnlineOrOffline;
import org.silverpeas.mobile.client.common.storage.LocalStorageHelper;
import org.silverpeas.mobile.shared.dto.comments.CommentDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: svu
 */
public class CommentsApp extends App implements CommentsAppEventHandler {

    private CommentsPage mainPage = new CommentsPage();

    public CommentsApp(String contentId, String instanceId, String contentType, String pageTitle, String title) {
        super();
        EventBus.getInstance().addHandler(AbstractCommentsAppEvent.TYPE, this);
        mainPage.setTitle(title);
        mainPage.setPageTitle(pageTitle);
        mainPage.setContentInfos(contentId, instanceId, contentType);
    }

    public void start(){
        setMainPage(mainPage);
        super.start();
    }

    @Override
    public void stop() {
        EventBus.getInstance().removeHandler(AbstractCommentsAppEvent.TYPE, this);
        super.stop();
    }

    @Override
    public void loadComments(final CommentsLoadEvent event) {
        final String key = "comments" + event.getContentType() + "_" + event.getContentId();
        AsyncCallbackOnlineOrOffline action = new AsyncCallbackOnlineOrOffline<List<CommentDTO>>(getOfflineAction(event, key)) {
            @Override
            public void onSuccess(List<CommentDTO> result) {
                super.onSuccess(result);
                LocalStorageHelper.store(key, List.class, result);
                EventBus.getInstance().fireEvent(new CommentsLoadedEvent(result));
            }

            @Override
            public void attempt() {
                ServicesLocator.getServiceComments().getComments(event.getContentId(), event.getContentType(), this);
            }
        };
        action.attempt();
    }

    private Command getOfflineAction(final CommentsLoadEvent event, final String key) {
        Command offlineAction = new Command() {

            @Override
            public void execute() {
                List<CommentDTO> result = LocalStorageHelper.load(key, List.class);
                if (result == null) {
                    result = new ArrayList<CommentDTO>();
                }
                EventBus.getInstance().fireEvent(new CommentsLoadedEvent(result));
            }
        };

        return offlineAction;
    }

    @Override
    public void addComment(final AddCommentEvent event) {
        AsyncCallbackOnlineOnly action = new AsyncCallbackOnlineOnly<CommentDTO>() {
            @Override
            public void attempt() {
                ServicesLocator.getServiceComments().addComment(event.getContentId(), event.getInstanceId(), event.getContentType(), event.getMessage(), this);
            }

            @Override
            public void onSuccess(final CommentDTO result) {
                super.onSuccess(result);
                EventBus.getInstance().fireEvent(new CommentAddedEvent(result));
            }
        };
        action.attempt();
    }
}