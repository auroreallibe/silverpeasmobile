package com.silverpeas.mobile.client.apps.media.pages.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.silverpeas.mobile.client.apps.media.events.pages.navigation.MediaItemClickEvent;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.resources.ApplicationMessages;
import com.silverpeas.mobile.shared.dto.media.PhotoDTO;

public class MediaItem extends Composite {

  private PhotoDTO data;
  private static MediaItemUiBinder uiBinder = GWT.create(MediaItemUiBinder.class);
  @UiField Anchor link;
  @UiField ImageElement thumb;
  protected ApplicationMessages msg = null;


  interface MediaItemUiBinder extends UiBinder<Widget, MediaItem> {
  }

  public MediaItem() {
    initWidget(uiBinder.createAndBindUi(this));
    msg = GWT.create(ApplicationMessages.class);
  }

  public void setData(PhotoDTO data) {
    this.data = data;
    link.setTitle(data.getTitle());
    thumb.setSrc(data.getDataPhoto());
    thumb.setAlt(data.getTitle());
  }

  @UiHandler("link")
  protected void onClick(ClickEvent event) {
    EventBus.getInstance().fireEvent(new MediaItemClickEvent(data));
  }
}
