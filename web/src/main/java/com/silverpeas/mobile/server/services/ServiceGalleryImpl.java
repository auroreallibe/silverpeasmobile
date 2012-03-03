package com.silverpeas.mobile.server.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;

import com.silverpeas.admin.ejb.AdminBm;
import com.silverpeas.gallery.ImageHelper;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.AlbumDetail;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;
import com.silverpeas.mobile.shared.dto.AlbumDTO;
import com.silverpeas.mobile.shared.dto.navigation.ApplicationInstanceDTO;
import com.silverpeas.mobile.shared.exceptions.AuthenticationException;
import com.silverpeas.mobile.shared.exceptions.GalleryException;
import com.silverpeas.mobile.shared.services.ServiceGallery;
import com.silverpeas.tags.util.EJBDynaProxy;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.JNDINames;

/**
 * Service de gestion des galleries d'images.
 * @author svuillet
 */
public class ServiceGalleryImpl extends AbstractAuthenticateService implements ServiceGallery {

	private final static Logger LOGGER = Logger.getLogger(ServiceGalleryImpl.class);
	private static final long serialVersionUID = 1L;
	private AdminBm adminBm;
	private GalleryBm galleryBm;
	
	/**
	 * Importation d'une image dans un album.
	 */
	public void uploadPicture(String name, String data, String idGallery, String idAlbum) throws GalleryException, AuthenticationException {
		checkUserInSession();
		
			
		String extension = ".jpg";
		if (data.indexOf("data:image/jpeg;base64,") != -1) {
			data = data.substring("data:image/jpeg;base64,".length());
			extension = ".jpg";
		}
		
		byte [] dataDecoded = org.apache.commons.codec.binary.Base64.decodeBase64(data.getBytes());
				
		try {			
			// stockage temporaire de la photo upload
			String tempDir = System.getProperty("java.io.tmpdir");		
			String filename = tempDir + File.separator + name + extension;
			OutputStream outputStream = new FileOutputStream(filename);
			
			// Remove metadata otherwise image upload in gallery crash (sanselan api too old, don't parse actual exif)
			ExifRewriter ER = new ExifRewriter();
			ER.removeExifMetadata(dataDecoded, outputStream); 
			//TODO : before remove metadata get orientation for make rotation if necessary
			
			outputStream.close();
						
			File file = new File(filename);
			
			// récupération de la configuration de la gallery			
			OrganizationController orga = new OrganizationController();
		    boolean watermark = "yes".equalsIgnoreCase(orga.getComponentParameterValue(idGallery, "watermark"));
		    boolean download = !"no".equalsIgnoreCase(orga.getComponentParameterValue(idGallery, "download"));
		    String watermarkHD = orga.getComponentParameterValue(idGallery, "WatermarkHD");
		    if(!StringUtil.isInteger(watermarkHD))  {
		      watermarkHD = "";
		    }
		    String watermarkOther = orga.getComponentParameterValue(idGallery, "WatermarkOther");
		     if(!StringUtil.isInteger(watermarkOther))  {
		      watermarkOther = "";
		    }			
			
		    // creation de la photo dans l'albums
			createPhoto(name, getUserInSession().getId(), idGallery, idAlbum, file, watermark, watermarkHD, watermarkOther, download);
			file.delete();
			
		} catch (Exception e) {
			LOGGER.error("uploadPicture", e);
		}	
	}
	
	private String createPhoto(String name, String userId, String componentId,
		      String albumId, File file, boolean watermark, String watermarkHD,
		      String watermarkOther, boolean download)
		      throws Exception {

		    // création de la photo
		    PhotoDetail newPhoto = new PhotoDetail(name, null, new Date(), null, null, null, download, false);
		    newPhoto.setAlbumId(albumId);
		    newPhoto.setCreatorId(userId);
		    PhotoPK pk = new PhotoPK("unknown", componentId);
		    newPhoto.setPhotoPK(pk);

		    String photoId = getGalleryBm().createPhoto(newPhoto, albumId);
		    newPhoto.getPhotoPK().setId(photoId);

		    // Création de la preview et des vignettes sur disque
		    ImageHelper.processImage(newPhoto, file, watermark, watermarkHD, watermarkOther);
		    ImageHelper.setMetaData(newPhoto, "fr");		    
		      
		    // Modification de la photo pour mise à jour dimension
		    getGalleryBm().updatePhoto(newPhoto);
		    return photoId;
		  }
	
	
	/**
	 * Retourne la listes des galleries accessibles.
	 */
	public List<ApplicationInstanceDTO> getAllGalleries() throws GalleryException, AuthenticationException {
		checkUserInSession();
		
		ArrayList<ApplicationInstanceDTO> results = new ArrayList<ApplicationInstanceDTO>();
		try {
			List<String> rootSpaceIds = getAdminBm().getAllRootSpaceIds();
			for (String rootSpaceId : rootSpaceIds) {
				List<String> componentIds = getAdminBm().getAvailCompoIds(rootSpaceId, getUserInSession().getId());
				for (String componentId : componentIds) {
					ComponentInstLight instance = getAdminBm().getComponentInstLight(componentId);
					if (instance.getName().equals("gallery")) {
						ApplicationInstanceDTO i = new ApplicationInstanceDTO();
						i.setId(instance.getId());
						i.setLabel(instance.getLabel());
						results.add(i);
					}
				}				
			}			
		} catch (Exception e) {
			LOGGER.error("getAllGalleries", e);
		}
		
		Collections.sort(results);		
		return results;		
	}
	
	/**
	 * Retourne la liste des albums d'une gallerie.
	 */
	public List<AlbumDTO> getAllAlbums(String instanceId) throws GalleryException, AuthenticationException {
		checkUserInSession();
		
		ArrayList<AlbumDTO> results = new ArrayList<AlbumDTO>();
		try {			
			Collection<AlbumDetail> albums = getGalleryBm().getAllAlbums(instanceId);
			for (AlbumDetail albumDetail : albums) {
				if (albumDetail.getLevel() != 1) {
					AlbumDTO album = new AlbumDTO();
					album.setId(String.valueOf(albumDetail.getId()));
					album.setName(albumDetail.getName());				
					results.add(album);
				}				
			}
		} catch (Exception e) {
			LOGGER.error("getAllAlbums", e);
		}
		return results;
	}
	
	private AdminBm getAdminBm() throws Exception {
		if (adminBm == null) {
			adminBm = (AdminBm) EJBDynaProxy.createProxy(JNDINames.ADMINBM_EJBHOME, AdminBm.class);
		}
		return adminBm;
	}
	
	private GalleryBm getGalleryBm() throws Exception {
		if (galleryBm == null) {
			galleryBm = (GalleryBm) EJBDynaProxy.createProxy(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
		}
		return galleryBm;
	}
}