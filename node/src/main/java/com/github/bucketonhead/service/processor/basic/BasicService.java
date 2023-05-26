package com.github.bucketonhead.service.processor.basic;

import com.github.bucketonhead.entity.AppUser;
import com.github.bucketonhead.service.processor.CommandProcessor;

public interface BasicService extends CommandProcessor {
    String processCancelCommand(AppUser user);

    String processHelpCommand();

    String processStartCommand(AppUser user);
}
