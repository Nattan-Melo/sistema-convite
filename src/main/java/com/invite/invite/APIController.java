package com.invite.invite;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
public class APIController {
    @Value("${application.baseUrl}")
    private String tmpUrl;


    @PostMapping("/api/include")
    public void includeNewGuest(@RequestBody GuestRequest guest)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference baseRef = database.getReference();
        DatabaseReference ref = baseRef.child("guests/");
        DatabaseReference newGuest = ref.push();
        Guest tmp = new Guest();
        tmp.setId(newGuest.getKey()); tmp.setName(guest.getName()); tmp.setLink(String.format("%s/convite/%s", tmpUrl,newGuest.getKey()));
        tmp.setAggregate(guest.getAggregate());
        newGuest.setValueAsync(tmp);
    }
    
    @DeleteMapping("/api/guests/{id}")
    public void removeGuest(@PathVariable String id)
    {      
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("guests/"+id);
        ref.removeValueAsync();            
                
    }

    @PutMapping("/api/guests/status")
    public void putMethodName(@RequestBody GuestStatus guest) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("guests/"+guest.getId());

        Map<String, Object> dsUpdate = new HashMap<>();
        dsUpdate.put("status", guest.getStatus());
        ref.updateChildrenAsync(dsUpdate);
    }

    @PostMapping("/api/guest")
    public ResponseEntity<?> guestResponse(@RequestBody GuestResponse guestReponse) {
        GuestStatus guestData = new GuestStatus();
        System.out.println("Resposta "+guestReponse.getResponse());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("guests").orderByKey().equalTo(guestReponse.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Guest tmp = snapshot.getValue(Guest.class);
                guestData.setId(tmp.getId()); 
                guestData.setStatus(tmp.getStatus());
            
                DatabaseReference ref = database.getReference().child("guests/"+guestReponse.getId());
                Map<String, Object> dsUpdate = new HashMap<>();
                if(guestReponse.getResponse() == 1)
                {
                    dsUpdate.put("status", 1);
                }
                else if(guestReponse.getResponse() == 0)
                {
                    dsUpdate.put("status", 2);
                }
                ref.updateChildrenAsync(dsUpdate);
               
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        
        if(guestData.getStatus() == 3 || guestData.getStatus() == 2)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.OK);
        }
  
    }
}
