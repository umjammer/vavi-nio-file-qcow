/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.nio.file.qcow;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.PointerByReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.libvirt.Connect;
import org.libvirt.Domain;
import vavi.nio.file.qcow.jna.libqcow.LibqcowLibrary;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


//@EnabledIf("localPropertiesExists")
@PropsEntity(url = "file:local.properties")
class Test1 {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String qcow = "src/test/resources/test.qcow";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
        assertTrue(Files.exists(Paths.get(qcow)));
    }

    @Test
    @DisplayName("libvirt")
    void test0() throws Exception {
        Connect conn = new Connect("file:///Users/nsano/.android/avd/Pixel_2_API_16.avd/sdcard.img.qcow2");
Debug.println(conn.nodeInfo().cores);
        for (String name : conn.listDefinedDomains()) {
            System.out.println(name);
            if (name != null) {
                Domain domain = conn.domainLookupByName(name);
                System.out.println(domain.getMaxMemory());
                System.out.println(domain.getUUIDString());
                System.out.println(domain.getInfo().maxMem);
                System.out.println(domain.getInfo().state);
                System.out.println(conn.listDomains().length);
            }
        }
    }

    @Test
    void test1() throws Exception {
        Debug.println("version: " + LibqcowLibrary.INSTANCE.libqcow_get_version());
Debug.println("qcow: " + qcow);

        PointerByReference file = new PointerByReference();
        PointerByReference error = new PointerByReference();

//        PointerByReference stderr = new PointerByReference();

        int r;
//        r = LibqcowLibrary.INSTANCE.libqcow_file_initialize(stderr, error);
//        assertEquals(1, r, "Unable to initialize stderr");
//Debug.println("stderr: " + stderr + ", " + stderr.getPointer());
//
//        r = LibqcowLibrary.INSTANCE.libqcow_file_open(stderr, "/dev/stderr", LibqcowLibrary.LIBQCOW_ACCESS_FLAG_WRITE, error);
//Debug.println("stderr: " + (char) error.getValue().getInt(0));
//        ByteBuffer bb = ByteBuffer.allocate(256);
//        r = LibqcowLibrary.INSTANCE.libqcow_error_sprint(error, bb, new NativeLong(256));
//Debug.println("stderr: " + r + ", " + new String(bb.array()));
//        assertEquals(1, r);

        r = LibqcowLibrary.INSTANCE.libqcow_file_initialize(file, error);
        assertEquals(1, r, "Unable to initialize file");

        r = LibqcowLibrary.INSTANCE.libqcow_file_open(file, qcow, LibqcowLibrary.LIBQCOW_ACCESS_FLAG_READ, error);
Debug.println("error: " + (char) error.getValue().getInt(0));
        assertEquals(1, r);
    }
}

/* */
