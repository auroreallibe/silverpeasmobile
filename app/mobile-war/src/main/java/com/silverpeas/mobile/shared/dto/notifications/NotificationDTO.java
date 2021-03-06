package com.silverpeas.mobile.shared.dto.notifications;

import com.silverpeas.mobile.shared.dto.BaseDTO;

import java.io.Serializable;

/**
 * @author: svu
 */
public class NotificationDTO extends BaseDTO implements Serializable {

  public final static String TYPE_PUBLICATION = "Publication";
  public final static String TYPE_PHOTO = "Photo";
  public final static String TYPE_VIDEO = "Video";
  public final static String TYPE_SOUND = "Sound";
  public final static String TYPE_STREAMING = "Streaming";

  private String contentId;
  private String contentType;
  private String message;
  private String instanceId;

  public NotificationDTO() {
    super();
  }

  public NotificationDTO(String message) {
    this.message = message;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }
}
