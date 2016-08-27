package mc.euro.demolition.util;

import java.lang.reflect.Field;
import java.util.List;

import mc.alk.arena.competition.Match;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Log;

/**
 * 
 * @author Nikolai
 */
public class MatchUtil {
    
    public static void setTime(Match match, long time) {
        try {
            Field mcField = match.getClass().getSuperclass().getDeclaredField("matchCountdown");
            mcField.setAccessible(true);
            Countdown cd = (Countdown) mcField.get(match);
            Field secField = cd.getClass().getDeclaredField("seconds");
            secField.setAccessible(true);
            secField.set(cd, time);
            
            Field vcsField = match.getClass().getSuperclass().getDeclaredField("vcs");
            vcsField.setAccessible(true);
            List<?> vcs = (List<?>) vcsField.get(match);
            for ( Object vc : vcs ) {
                if (vc instanceof TimeLimit) {
                    Field timerField = vc.getClass().getDeclaredField("timer");
                    timerField.setAccessible(true);
                    Countdown timer = (Countdown) timerField.get(vc);
                    Field secondsField = timer.getClass().getDeclaredField("seconds");
                    secondsField.setAccessible(true);
                    secondsField.set(timer, time);
                }
            }
        } catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex)  {
            Log.err( MatchUtil.class.getName() + ex.getStackTrace() );        
        } 
    }
    
    public static void continueMatchOnExpiration( Match match ) { setCancelOnExpire( match, false ); }
    
    public static void stopMatchOnExpiration( Match match ) { setCancelOnExpire( match, true ); }
    
    public static void setCancelOnExpire( Match match, boolean cancel ) {
        try {
            Field mc = match.getClass().getSuperclass().getDeclaredField("matchCountdown");
            mc.setAccessible(true);
            Countdown cd = (Countdown) mc.get(match);
            cd.setCancelOnExpire(cancel);
            
            Field vcsField = match.getClass().getSuperclass().getDeclaredField("vcs");
            vcsField.setAccessible(true);
            List<?> vcs = (List<?>) vcsField.get(match);
            for ( Object vc : vcs ) {
                if (vc instanceof TimeLimit) {
                    Field timerField = vc.getClass().getDeclaredField("timer");
                    timerField.setAccessible(true);
                    Countdown timer = (Countdown) timerField.get(vc);
                    timer.setCancelOnExpire(cancel);
                }
            }
        } catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex)  {
            Log.err( MatchUtil.class.getName() + ex.getStackTrace() );        
        } 
    }
}
