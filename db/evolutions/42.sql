#
# versione base database CINI
#

# --- !Ups

-- schema update # 1
	ALTER TABLE Grammar MODIFY COLUMN grammarBibliographicalReference LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL ;

-- schema update # 2

	ALTER TABLE Grammar MODIFY COLUMN grammarISBNInfo LONGTEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL ;

	