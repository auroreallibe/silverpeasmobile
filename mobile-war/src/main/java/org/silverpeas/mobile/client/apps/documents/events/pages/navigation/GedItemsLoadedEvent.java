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

package org.silverpeas.mobile.client.apps.documents.events.pages.navigation;

import org.silverpeas.mobile.shared.dto.BaseDTO;

import java.util.List;

public class GedItemsLoadedEvent extends AbstractGedNavigationPagesEvent {

	private List<BaseDTO> topicsAndPublications;
	private boolean forceReload = false;

	private int sharing;
	private boolean canImport = false;
	
	public GedItemsLoadedEvent(List<BaseDTO> topicsAndPublications, int sharing, boolean canImport) {
		super();
		this.topicsAndPublications = topicsAndPublications;
		this.sharing = sharing;
		this.canImport = canImport;
	}

	public GedItemsLoadedEvent(List<BaseDTO> topicsAndPublications, int sharing, boolean canImport, boolean forceReload) {
		super();
		this.topicsAndPublications = topicsAndPublications;
		this.sharing = sharing;
		this.canImport = canImport;
		this.forceReload = forceReload;
	}

	@Override
	protected void dispatch(GedNavigationPagesEventHandler handler) {
		handler.onLoadedTopics(this);
	}

	public List<BaseDTO> getTopicsAndPublications() {
		return topicsAndPublications;
	}

	public int getSharing() {
		return sharing;
	}

	public boolean isForceReload() { return forceReload; }

	public boolean isCanImport() { return canImport; }
}
