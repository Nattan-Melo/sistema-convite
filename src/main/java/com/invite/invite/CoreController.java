package com.invite.invite;
// Java Utils imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
// Springboot framework imports
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


// Firebase Admin imports
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


@Controller
public class CoreController {

    @Value("${application.baseUrl}")
    private String baseUrl;

    private List<Guest> guests = new ArrayList<Guest>();
    

    private GuestFrontendModel taskGetGuestData(String guestId)
    {
        GuestFrontendModel guestResponse = new GuestFrontendModel();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("guests").orderByKey().equalTo(guestId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                if(snapshot.exists())
                {
                    Guest guest = snapshot.getValue(Guest.class);
                    guestResponse.setId(guest.getId());
                    guestResponse.setName(guest.getName());
                    guestResponse.setAggregate(guest.getAggregate());
                    guestResponse.setStatus(guest.getStatus());
                }
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
        return guestResponse;
    }

    @GetMapping("/convite/{guestId}")
    public String initialPage(@PathVariable(required=true) String guestId,Model model) throws InterruptedException, ExecutionException, TimeoutException
    {  
        ExecutorService taskManager = Executors.newCachedThreadPool();
        Future<GuestFrontendModel> task = taskManager.submit(()-> taskGetGuestData(guestId));
        GuestFrontendModel guest = task.get(2000, TimeUnit.MILLISECONDS);
        taskManager.shutdown();
        // System.out.println(guest.getId());
        if((guest.getId() == "" )||(guest.getName() == ""))
        {
            return "error";
        }
        else
        {
            if(guest.getStatus() == 2 || guest.getStatus() == 3)
            {
                return "contact";
            }
            else if(guest.getStatus() == 1)
            {
                return "accept";
            }
            else
            {
                model.addAttribute("guestData", guest);
                model.addAttribute("inviteResponse", new GuestResponse());
                return "index";
            }
        } 
    }


    private List<Guest> getAllGuests()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        List<Guest> tmp = new ArrayList<Guest>();

        database.getReference().child("guests").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot obj : snapshot.getChildren()) {
                    tmp.add(obj.getValue(Guest.class));
                    
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'onCancelled'");
            }
            
        });

        return tmp;
    }

    private Map<String,Integer> taskCountGuestData(List<Guest> guests)
    {
        int waitingStat   = 0;
        int acceptedStat  = 0;
        int cancelledStat = 0;
        int refusedStat   = 0;

        for (Guest obj : guests) {
            switch(obj.getStatus())
            {
                case 0:
                {
                    waitingStat++;
                }break;
                case 1:
                {
                    acceptedStat++;
                }break;
                case 2:
                {
                    refusedStat++;
                    
                }break;
                case 3:
                {
                    cancelledStat++;
                }break;
            }
        }
        
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("waiting", waitingStat);
        statsMap.put("accepted", acceptedStat);
        statsMap.put("cancelled", cancelledStat);
        statsMap.put("refused", refusedStat);
        return statsMap;
    }

    @GetMapping("/panel")
    public String controlPanel(Model model) throws InterruptedException, ExecutionException, TimeoutException {
        
        this.guests = new ArrayList<Guest>();
        ExecutorService taskManager = Executors.newCachedThreadPool();
        Future<List<Guest>> task = taskManager.submit(() -> getAllGuests());
        this.guests = task.get(5000, TimeUnit.MILLISECONDS);
        Future<Map<String, Integer>> taskCount = taskManager.submit(() -> taskCountGuestData(this.guests));
        
        taskManager.shutdown();

        model.addAttribute("STATcount", taskCount.get());
        model.addAttribute("guests", guests);
        model.addAttribute("baseUrl",baseUrl);
        return "panel";
    }

    @PostMapping("/convite/resposta")
    public String getInvitationResponse(@ModelAttribute GuestResponse inviteResponse, Model model)
    {
        model.addAttribute("inviteResponse", inviteResponse);

        GuestFrontendModel guestFrontend = new GuestFrontendModel();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("guests").orderByKey().equalTo(inviteResponse.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                DatabaseReference ref = database.getReference().child("guests/"+inviteResponse.getId());
                Map<String, Object> dsUpdate = new HashMap<>();
                if(inviteResponse.getResponse() == 1)
                {
                    dsUpdate.put("status", 1);
                }
                else if(inviteResponse.getResponse() == 0)
                {
                    dsUpdate.put("status", 2);
                }
                ref.updateChildrenAsync(dsUpdate);
                
                database.getReference("guests").orderByKey().equalTo(inviteResponse.getId()).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                        if(snapshot.exists())
                        {
                            Guest guest = snapshot.getValue(Guest.class);
                            guestFrontend.setId(guest.getId());
                            guestFrontend.setName(guest.getName());
                            guestFrontend.setAggregate(guest.getAggregate());
                            guestFrontend.setStatus(guest.getStatus());
                        }
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
        
        
        switch(guestFrontend.getStatus()) {
            case 0: // Esperando
            {
                model.addAttribute("guestData", guestFrontend);
                return "accept";
                // Accepted page
            }
            case 1: // Aceito
            {
                model.addAttribute("guestData", guestFrontend);
                return "invite";
            

            }
            case 2: // Recusado
            {
                return "refuse";
            
            }
            case 3: // Cancelado
            {
                return "contact";
                // Contact page
                
            }
            default:
                return "error";
        }
        
    }

    

}
