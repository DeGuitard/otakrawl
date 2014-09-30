package com.deguitard.otakrawl;

/**
 * <p>Enumeration of the different possible import types.</p>
 * <p>Here the list:</p>
 * <ul><li><u>FULL</u>: imports all mangas possible, with all the chapters.</li>
 * <li><u>UPDATE</u>: only updates the existing mangas.</li>
 * <li><u>SUGGESTIONS</u>: imports/updates the suggestions for all mangas.</ul>
 *
 * @author Vianney Dupoy de Guitard
 *
 */
public enum ImportType {

	/** A full import consists of importing all mangas possibles, including the chapters. */
	FULL,

	/** An update import consists of updating all existing mangas, including new chapters. */
	UPDATE,

	/** A suggestions import consists of gathering all recommendations for all mangas. */
	SUGGESTIONS;
}
