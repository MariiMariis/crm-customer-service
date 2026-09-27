package com.pb.crm.commons.audit;

import com.pb.crm.commons.actor.RequestActor;
import org.hibernate.envers.RevisionListener;

public class CrmRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        ((CrmRevisionEntity) revisionEntity).setActor(RequestActor.current());
    }
}
