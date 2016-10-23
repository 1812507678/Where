package haijun.root.where.bean;

import java.util.List;

/**
 * Created by root on 10/23/16.
 */

public class HistoryLocation {
    public String status;
    public String size;
    public String total;
    public String entity_name;
    public String distance;
    public List<LocationPoint> points;

    public class LocationPoint{
        public String loc_time;
        public String []location;
        public String create_time;
        public String radius;
        public String speed;
        public String direction;
    }
}
