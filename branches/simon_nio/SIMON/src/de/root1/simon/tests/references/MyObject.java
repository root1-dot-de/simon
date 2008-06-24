package de.root1.simon.tests.references;
import java.lang.ref.*;
class MyObject
{
  protected void finalize() throws Throwable
  {
    System.err.println("In finalize method for this object: " + this);
  }
}

