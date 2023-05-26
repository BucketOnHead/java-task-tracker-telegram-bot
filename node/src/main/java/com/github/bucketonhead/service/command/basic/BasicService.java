package com.github.bucketonhead.service.command.basic;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.command.CommandProcessor;

public interface BasicService extends CommandProcessor {
    String processCancelCommand(AppUser user);

    String processHelpCommand();

    String processStartCommand(AppUser user);
}
