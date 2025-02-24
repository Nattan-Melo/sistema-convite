package com.invite.invite;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestInviteResponse {
    private String id;
    private int response;

    @Override
    public String toString() {
        return String.format("-> ID %s and response %d", this.id, this.response);
    }
    
}
