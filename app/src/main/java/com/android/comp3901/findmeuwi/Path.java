package com.android.comp3901.findmeuwi;

import android.database.Cursor;
import android.util.Log;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created on 2/17/2017.
 */

public class Path {

    // Used in Path Finding method
    private List<Vertex> nodes;
    private static List<Vertex> connectedNodes;
    private List<Edge> edges;
    DB_Helper dbHelper;

    private Graph graph;
    static HashMap<String,Vertex> vertices;


    /*
     *  Constructor methods which initializes and creates all the vertices and edges.
     */
    public Path(DB_Helper db){

        this.nodes = new ArrayList<Vertex>();
        this.connectedNodes = new ArrayList<Vertex>();
        this.edges = new ArrayList<Edge>();
        this.vertices = new HashMap<String,Vertex>();
        this.dbHelper = db;

        Cursor verticesDB = dbHelper.getVertices();
        Cursor edgesDB = dbHelper.getEdges();


        //creates a vertex object for the values in the database
        while (!verticesDB.isAfterLast()) {
            Vertex location = creator(verticesDB);

            //Places the create vertex into a list and a HashMap
            nodes.add(location);
            vertices.put(verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_ID)), location);

            verticesDB.moveToNext();
        }

        //creates a edge for each value in the database
        while ( !edgesDB.isAfterLast() ){

           Vertex v1 = vertices.get(edgesDB.getString(0));
           Vertex v2 =  vertices.get(edgesDB.getString(1));


            //creates the same edge twice, for going foward and one for going backwards
            Edge lane1 = new Edge(edgesDB.getString(0) + " -> " + edgesDB.getString(1),
                    vertices.get(edgesDB.getString(0)),
                    vertices.get(edgesDB.getString(1)),
                    (int) edgesDB.getDouble(2));

            Edge lane2 = new Edge(edgesDB.getString(1) + " -> " + edgesDB.getString(0),
                    vertices.get(edgesDB.getString(1)),
                    vertices.get(edgesDB.getString(0)),
                    (int) edgesDB.getDouble(2));


            connectedNodes.add(vertices.get(edgesDB.getString(0)));
            connectedNodes.add(vertices.get(edgesDB.getString(1)));

            edges.add(lane1);
            edges.add(lane2);

            edgesDB.moveToNext();
        }

        this.graph = new Graph(nodes,edges);
    }


    /*
     * Uses the dijkstra to generates the shortest path
     *
     */
    public LinkedList<Vertex>   getPath(String src, String dest) {

        DijkstraAlgorithm  dijkstra = new DijkstraAlgorithm(graph);

        dijkstra.execute(vertices.get(src));

        LinkedList<Vertex> path = dijkstra.getPath(vertices.get(dest));



        //assertNotNull(path);
        //assertTrue(path.size() > 0);

        return path;
    }

    public HashMap<String,Vertex> getVertices(){ return vertices; };

    public Graph getGraph(){
        return graph;
    }

    public List getCNodes() {   return  connectedNodes; }

    public List<Vertex> getNodes(){
        return nodes;
    }

    public List<Edge> getEdges(){
        return edges;
    }



    /*
     *Used when creating the vertices. Used to create the vertices of the right subclass
     */
    private Vertex creator(Cursor verticesDB){

        Vertex Location = null;
        String type =verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_TYPE)).toLowerCase().replaceAll("\\s+","");
        String test =verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_ID));

        switch (type){

            case "room":
                Cursor res = dbHelper.findClasses( verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_ID)) );

                if(res.getCount() == 0){
                    Log.d("GET ROOM", test);
                    assertEquals(1,res.getCount());
                }

                Location = new Room(res.getString(res.getColumnIndex(DB_Helper.RT_ID)),
                        res.getString(res.getColumnIndex(DB_Helper.RT_NAME)),//name
                        res.getDouble(res.getColumnIndex(DB_Helper.RT_LAT)), //latitude
                        res.getDouble(res.getColumnIndex(DB_Helper.RT_LONG)),//longtude
                        res.getDouble(res.getColumnIndex(DB_Helper.RT_FLOOR))
                        );
                break;

//            case "building":
//                //Location = new Building()
//                        break;
            default:
                Location = new Vertex(verticesDB.getString(0),  //Vid
                        verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_NAME)),//Vname
                        verticesDB.getDouble(verticesDB.getColumnIndex(DB_Helper.V_LAT)), //latitude
                        verticesDB.getDouble(verticesDB.getColumnIndex(DB_Helper.V_LONG)),//longtude
                        verticesDB.getString(verticesDB.getColumnIndex(DB_Helper.V_TYPE)));//type
                break;
        }


        return Location;
    }

}
