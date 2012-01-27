package com.silverpeas.mobile.client.pages.gallery;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtmobile.persistence.client.Collection;
import com.gwtmobile.persistence.client.CollectionCallback;
import com.gwtmobile.persistence.client.Entity;
import com.gwtmobile.persistence.client.Persistence;
import com.gwtmobile.persistence.client.ScalarCallback;
import com.gwtmobile.phonegap.client.Camera;
import com.gwtmobile.phonegap.client.Notification;
import com.gwtmobile.ui.client.page.Page;
import com.gwtmobile.ui.client.page.Transition;
import com.gwtmobile.ui.client.widgets.Button;
import com.gwtmobile.ui.client.widgets.DropDownItem;
import com.gwtmobile.ui.client.widgets.DropDownList;
import com.gwtmobile.ui.client.widgets.HeaderPanel;
import com.gwtmobile.ui.client.widgets.HorizontalPanel;
import com.silverpeas.mobile.client.common.Database;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.ServicesLocator;
import com.silverpeas.mobile.client.common.event.ErrorEvent;
import com.silverpeas.mobile.client.components.icon.Icon;
import com.silverpeas.mobile.client.pages.gallery.browser.PicturePage;
import com.silverpeas.mobile.client.pages.gallery.browser.remote.GalleryRemoteBrowser;
import com.silverpeas.mobile.client.pages.gallery.controler.GalleryControler;
import com.silverpeas.mobile.client.pages.gallery.controler.event.AbstractGalleryEvent;
import com.silverpeas.mobile.client.pages.gallery.controler.event.GalleryEventHandler;
import com.silverpeas.mobile.client.pages.gallery.controler.event.GalleryLoadedSettingsEvent;
import com.silverpeas.mobile.client.persist.Picture;
import com.silverpeas.mobile.shared.dto.AlbumDTO;
import com.silverpeas.mobile.shared.dto.ApplicationInstanceDTO;
import com.silverpeas.mobile.client.components.Navigation;

/**
 * Pictures gallery mobile application.
 * @author svuillet
 */
public class GalleryPage extends Page implements GalleryEventHandler {

	private static GalleryPageUiBinder uiBinder = GWT.create(GalleryPageUiBinder.class);
	private GalleryControler controler = new GalleryControler();
	
	@UiField protected Icon takePicture, local, sync, remote;
	@UiField protected HeaderPanel footer, header;
	@UiField protected HorizontalPanel content;
	@UiField protected DropDownList galleries, albums;
	@UiField protected Label footerTitle;
	@UiField protected Button navButton;
	@UiField protected HTMLPanel htmlPanel;
	
	private static int nbPictures;
	private static int ratioPicture;
	private static boolean uploading, stopScheduler;
	
	interface GalleryPageUiBinder extends UiBinder<Widget, GalleryPage> {
	}

	public GalleryPage() {
		initWidget(uiBinder.createAndBindUi(this));
		EventBus.getInstance().addHandler(AbstractGalleryEvent.TYPE, this);
		
		Element select = galleries.getElement().getElementsByTagName("select").getItem(0);
		select.getStyle().setDisplay(Display.BLOCK);
		select.getStyle().setWidth(100, Unit.PCT);
		select.getStyle().setHeight(44, Unit.PX);
		select.getNextSiblingElement().getStyle().setHeight(44, Unit.PX);
		select = albums.getElement().getElementsByTagName("select").getItem(0);		
		select.getStyle().setDisplay(Display.BLOCK);
		select.getStyle().setWidth(100, Unit.PCT);
		select.getStyle().setHeight(44, Unit.PX);
		select.getNextSiblingElement().getStyle().setHeight(44, Unit.PX);
	}
	
	/**
	 * Get all galleries instances.
	 */
	@Override
	protected void onNavigateTo() {		
		Notification.activityStart();
		galleries.getListBox().clear();		
		ServicesLocator.serviceGallery.getAllGalleries(new AsyncCallback<List<ApplicationInstanceDTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				Notification.activityStop();
				// TODO display specific error instead of generic
				EventBus.getInstance().fireEvent(new ErrorEvent(new Exception(caught)));
			}

			@Override
			public void onSuccess(List<ApplicationInstanceDTO> result) {
				DropDownItem d = new DropDownItem();
				galleries.add(d);				
				for (ApplicationInstanceDTO gallery : result) {
					d = new DropDownItem();
					d.setText(gallery.getLabel());
					d.setValue(gallery.getId());
					galleries.add(d);
				}
				controler.getSettings();
				Notification.activityStop();
			}
			
		});
		
		super.onNavigateTo();
	}
	
	/**
	 * Get all albums on new gallery selection.
	 * @param e
	 */
	@UiHandler("galleries")
	void onGalleryChange(ValueChangeEvent<String> e) {
		getAlbums(e.getValue(), null);
	}
	
	private void getAlbums(final String galleryId, final String selectedGalleryId) {
		ServicesLocator.serviceGallery.getAllAlbums(galleryId, new AsyncCallback<List<AlbumDTO>>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO display specific error instead of generic
				EventBus.getInstance().fireEvent(new ErrorEvent(new Exception(caught)));				
			}

			@Override
			public void onSuccess(List<AlbumDTO> result) {
				int i = 0;
				int index = -1;
				albums.getListBox().clear();
				DropDownItem emptyA = new DropDownItem();
				albums.add(emptyA);
				for (AlbumDTO album : result) {
					DropDownItem a = new DropDownItem();
					a.setText(album.getName());
					a.setValue(album.getId());
					albums.add(a);
					if (selectedGalleryId != null) {
						if (selectedGalleryId.equals(album.getId())) {
							index = i;
						}
					}
					i++;
				}
				if (index != -1) albums.getListBox().setSelectedIndex(index+1);
			}
		});
	}
	
	/**
	 * Store in html5 database album selected.
	 * @param e
	 */
	@UiHandler("albums")
	void onAlbumChange(ValueChangeEvent<String> e) {
		if (!e.getValue().isEmpty()) {
			controler.saveOrUpdateSettings(galleries.getSelectedValue(), albums.getSelectedValue());			
		}
	}	
	
	/**
	 * Take a picture and store it in local database.
	 * @param e
	 */
	@UiHandler("takePicture")
	void takePicture(ClickEvent e) {
		Camera.Options options = new Camera.Options();
		options.quality(50);
		//options.destinationType(DestinationType.FILE_URI); // for optimal performances
		
		Camera.getPicture(new Camera.Callback() {			
			public void onSuccess(final String imageData) {				
				Notification.activityStart();
				Database.open();		
				final Entity<Picture> pictureEntity = GWT.create(Picture.class);				
				Persistence.schemaSync(new com.gwtmobile.persistence.client.Callback() {			
					public void onSuccess() {
						final Picture pic = pictureEntity.newInstance();
						pic.setData(imageData);
						Persistence.flush();
						Notification.activityStop();
					}
				});
			}

			public void onError(String message) {
				// TODO : manage cancel photo taking
				Notification.activityStop();
				EventBus.getInstance().fireEvent(new ErrorEvent(new Exception(message)));	
			}
		}, options);
	}
	
	/**
	 * Browser local pictures.
	 * @param e
	 */
	@UiHandler("local")
	void localPictures(ClickEvent e){
		Database.open();
		final Entity<Picture> pictureEntity = GWT.create(Picture.class);
		final Collection<Picture> pictures = pictureEntity.all();
		pictures.count(new ScalarCallback<Integer>() {					
			@Override
			public void onSuccess(final Integer count) {
				if (count == 0) {
					Notification.alert("No locals pictures", null, "Information", "OK");
				} else {
					Notification.activityStart();
					pictures.list((new CollectionCallback<Picture>(){

						@Override
						public void onSuccess(Picture[] pictures) {
							final PicturePage picturePage = new PicturePage();
							picturePage.setPictures(pictures);
							Notification.activityStop();
							goTo(picturePage, Transition.SLIDE);
						}
						
					}));				
				}
			}
		});		
	}
	
	/**
	 * Send local pictures to server
	 * @param e
	 */
	@UiHandler("sync")
	void syncPictures(ClickEvent e){
		Database.open();
		final Entity<Picture> pictureEntity = GWT.create(Picture.class);
		final Collection<Picture> pictures = pictureEntity.all();
		pictures.count(new ScalarCallback<Integer>() {
			
			@Override
			public void onSuccess(final Integer count) {
				
				if (count > 0) {
					Notification.progressStart("Uploading", count + " pictures");
					nbPictures = 0;	
					ratioPicture = 100 / count;
					pictures.list(new CollectionCallback<Picture>() {
						@Override
						public void onSuccess(Picture[] results) {
							uploadPicture(count, results, pictures);					
						}					
					});
				} else {
					Notification.alert("Nothing to upload", null, "Information", "OK");
				}					
			}
		});		
		
		
	}
	
	/**
	 * Upload one picture to gallery.
	 * @param count
	 */
	private void uploadPicture(final Integer count, final Picture[] results, final Collection<Picture> pictures) {
		
		/*
		// For optimal performances
		FileReader reader = File.newReaderInstance();
		reader.readAsDataURL("");
		reader.onLoadEnd(callback);
		*/		
		
		uploading = false;
		stopScheduler = false;
		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {								
			@Override
			public boolean execute() {					
				
				if (uploading == false) {					
					for (int i = 0; i < results.length; i++) {
						final Picture picture = results[i];				
					
						String name = picture.getName();
						if (picture.getName() == null || picture.getName().isEmpty()) {
							name = picture.getId();
						}
						
						uploading = true;
						ServicesLocator.serviceGallery.uploadPicture(name, picture.getData(), new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								EventBus.getInstance().fireEvent(new ErrorEvent(new Exception(caught)));
								stopScheduler = true;
							}

							@Override
							public void onSuccess(Void result) {
								// remove picture from html5 bdd
								pictures.remove(picture);
								
								// compute job progress
								nbPictures++;
								Notification.progressValue(nbPictures*ratioPicture);								
								if (count > nbPictures) {
									uploading = false;
								} else {
									Notification.progressStop();
									stopScheduler = true;
								}								
							}
						});						
					}										
				}				
				return stopScheduler;
			}
		}, 300);
	}
	
	/**
	 * Browse remote galleries.
	 * @param e
	 */
	@UiHandler("remote")
	void remotePictures(ClickEvent e) {
		final GalleryRemoteBrowser remoteBrowser = new GalleryRemoteBrowser();
		goTo(remoteBrowser, Transition.SLIDE);
	}

	@Override
	public void onLoadedSettings(GalleryLoadedSettingsEvent event) {
		String galleryId = event.getSettings().getSelectedGalleryId();		
		for (int i = 0; i < galleries.getListBox().getItemCount(); i++) {
			if (galleries.getListBox().getValue(i).equals(galleryId)) {
				galleries.getListBox().setSelectedIndex(i);
				break;
			}		
		}
		getAlbums(galleryId, event.getSettings().getSelectedAlbumId());
	}	
	
	@UiHandler("navButton")
	void Navigation(ClickEvent e){
		Navigation navigation = new Navigation("gallery", 0);
		htmlPanel.clear();
		htmlPanel.add(navigation);
	}
}
