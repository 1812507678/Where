package haijun.root.where.bean;

/**
 * Created by root on 10/23/16.
 */

public class LocationInformation  {
    private String status;
    public String message;
    private String size;
    private String total;
    public Location []entities;

    public class Location{
        private String entity_name;
        private String create_time;
        private String modify_time;
        public TimePoint realtime_point;

        public class TimePoint{
            private String loc_time;
            public String []location;
            private String radius;
            private String speed;
            private String direction;

        }
    }

}
