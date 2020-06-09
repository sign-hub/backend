#
# versione base database CINI
#

# --- !Ups

-- schema update # 1

    alter table Grammar 
        add column htmlPath varchar(255);

-- schema update # 2

    alter table Grammar 
        add column pdfPath varchar(255);
        
# --- !Downs
