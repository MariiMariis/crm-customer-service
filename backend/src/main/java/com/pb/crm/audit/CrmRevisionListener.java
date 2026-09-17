package com.pb.crm.audit;

import com.pb.crm.config.RequestActor;
import org.hibernate.envers.RevisionListener;

public class CrmRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        ((CrmRevisionEntity) revisionEntity).setActor(RequestActor.current());
    }
}
